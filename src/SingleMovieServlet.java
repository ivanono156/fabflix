import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        request.getServletContext().log("getting id " + id);
        PrintWriter out = response.getWriter();

        String query = "select m.id, m.title, m.year, m.director, r.rating, " +
                "g.id, g.name, smp.id, smp.name, smp.moviesPlayed " +
                "from movies as m inner join stars_in_movies as sim on m.id = sim.movieId " +

                // Find how many movies each star played in (starMoviesPlayed as smp)
                "inner join (select s.id, s.name, count(s.id) as moviesPlayed " +
                // Filter out only stars that acted in this movie first
                "from (select s.id, s.name from stars as s " +
                "inner join stars_in_movies as sim on s.id = sim.starId " +
                "inner join movies as m on sim.movieId = m.id " +
                "where m.id = ?) as s " +
                "inner join stars_in_movies as sim on s.id = sim.starId " +
                "group by (s.id)) as smp on sim.starId = smp.id " +

                // Find the genres in the movie
                "inner join genres_in_movies as gim on gim.movieId = m.id " +
                "inner join genres as g on g.id = gim.genreId " +

                // Find ratings for the movie
                "left join ratings as r on r.movieId = m.id " +
                "where m.id = ? " +
                "order by g.name, moviesPlayed desc, smp.name";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, id);
            statement.setString(2, id);

            try (ResultSet rs = statement.executeQuery()) {
                JsonObject jsonObject = new JsonObject();
                JsonObject genres = new JsonObject();
                JsonObject stars = new JsonObject();

                while (rs.next()) {
                    String movieId = rs.getString("m.id");
                    String movieTitle = rs.getString("m.title");
                    String movieYear = rs.getString("m.year");
                    String movieDirector = rs.getString("m.director");
                    String movieRating = rs.getString("r.rating");

                    String genreId = rs.getString("g.id");
                    String genreName = rs.getString("g.name");

                    String starId = rs.getString("smp.id");
                    String starName = rs.getString("smp.name");

                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_rating", movieRating);

                    genres.addProperty(genreId, genreName);
                    stars.addProperty(starId, starName);
                }

                jsonObject.add("movie_genres", genres);
                jsonObject.add("movie_stars", stars);

                out.write(jsonObject.toString());
                // Set response status to 200 (OK)
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}