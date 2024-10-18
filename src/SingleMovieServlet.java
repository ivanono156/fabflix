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
        // Response MIME type
        response.setContentType("application/json");
        // Retrieve parameter id from the url
        String id = request.getParameter("id");
        // Log message can be found in localhost log
        request.getServletContext().log("getting id " + id);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from the database
        try (Connection conn = dataSource.getConnection()) {
            // Construct query

            String query1 = "select * " +
                    "from (((((movies as m " +
                    "inner join stars_in_movies as sim on m.id = sim.movieId) " +
                    "inner join stars as s on s.id = sim.starId) " +
                    "left join genres_in_movies as gim on gim.movieId = m.id) " +
                    "left join genres as g on gim.genreId = g.id) " +
                    "left join ratings as r on r.movieId = m.id) " +
                    "where m.id = ? " +
                    "order by g.name";

            String starsByMoviesPlayedQuery = "select s.id, s.name, count(s.id) as moviesPlayed " +
                    "from stars as s inner join stars_in_movies as sim on s.id = sim.starId " +
                    "group by (s.id) " +
                    "order by moviesPlayed desc, s.name";

            String genresInMovieQuery = "select g.id, g.name, m.id " +
                    "from genres as g inner join genres_in_movies as gim on g.id = gim.genreId " +
                    "inner join movies as m on gim.movieId = m.id " +
                    "where m.id = ?" +
                    "order by g.name";

            String query = "select m.id, m.title, m.year, m.director, r.rating, " +
                    "g.id, g.name, smp.id, smp.name, smp.moviesPlayed " +
                    "from movies as m inner join stars_in_movies as sim on m.id = sim.movieId " +

                    // Find how many movies each star played in (starMoviesPlayed as smp)
                    "inner join (select s.id, s.name, count(s.id) as moviesPlayed " +
                    "from stars as s inner join stars_in_movies as sim on s.id = sim.starId " +
                    "group by (s.id)) as smp on sim.starId = smp.id " +

                    // Find the genres in the movie
                    "inner join genres_in_movies as gim on gim.movieId = m.id " +
                    "inner join genres as g on g.id = gim.genreId " +

                    // Find ratings for the movie
                    "left join ratings as r on r.movieId = m.id " +
                    "where m.id = ? " +
                    "order by g.name, moviesPlayed desc, smp.name";


            // Declare statement
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                // Set the parameter to the given movie id
                statement.setString(1, id);

                // Perform the query
                try (ResultSet rs = statement.executeQuery()) {
                    JsonObject jsonObject = new JsonObject();
                    JsonObject genres = new JsonObject();
                    JsonObject stars = new JsonObject();

                    // Iterate through each row of the result set
                    while (rs.next()) {
                        // Get movie info
                        String movieId = rs.getString("m.id");
                        String movieTitle = rs.getString("m.title");
                        String movieYear = rs.getString("m.year");
                        String movieDirector = rs.getString("m.director");
                        String movieRating = rs.getString("r.rating");
                        // Get genre info
                        String genreId = rs.getString("g.id");
                        String genreName = rs.getString("g.name");
                        // Get star info
                        String starId = rs.getString("smp.id");
                        String starName = rs.getString("smp.name");

                        jsonObject.addProperty("movie_id", movieId);
                        jsonObject.addProperty("movie_title", movieTitle);
                        jsonObject.addProperty("movie_year", movieYear);
                        jsonObject.addProperty("movie_director", movieDirector);
                        jsonObject.addProperty("movie_rating", movieRating);
                        // Add genres to json object
                        genres.addProperty(genreId, genreName);
                        // Add stars to json object
                        stars.addProperty(starId, starName);
                    }

                    jsonObject.add("movie_genres", genres);
                    jsonObject.add("movie_stars", stars);

                    // Write JSON string to output
                    out.write(jsonObject.toString());
                    // Set response status to 200 (OK)
                    response.setStatus(200);
                }
            }
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}