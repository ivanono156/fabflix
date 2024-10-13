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

// Declaring a web servlet called movie list servlet, which maps to url "/api/movie-list"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {

    // Create a data source register in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Response MIME type
        response.setContentType("application/json");
        // Retrieve parameter id from the url
        // Log message can be found in localhost log
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from the database
        try (Connection conn = dataSource.getConnection()) {
            // Construct query
            String query = "select m.title, m.year, m.director, " +
                    " select group_concat(g.name group by g.name limit 3) " +
                    "from genres"
                    + "join genres_in_movies ON genres.id = genres_in_movies.genre_id "
                    + " where genres_in_movies.movieId = movies.id) as limit 3"
                    +"group_concat(s.name group by s.name limit 3) as stars, r.rating"
                    + "from movies m"
                    + "join ratings r ON m.id = r.movieId"
                    + "join genres_in_movies gim ON gim.id = m.genreId"
                    + "join genre g ON g.id = gim.genreId"
                    + "join stars_in_movies sim ON sim.movieId = m.id"
                    + "join stars s ON s.id = sim.starId"
                    + "group by m.title, m.year, m.director"
                    + "order by r.rating desc " + "limit 20";
            // Declare statement
            try (PreparedStatement statement = conn.prepareStatement(query)){

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
                        String starId = rs.getString("s.id");
                        String starName = rs.getString("s.name");

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