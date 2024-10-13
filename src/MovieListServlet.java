import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
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

            String query = "select m.id, m.title , m.year, m.director, " +
                    "(select group_concat(distinct g.name order by g.name separator ', ') "
                    +"from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + " where gim.movieId = m.id "
                    + "limit 3) " + "as genres, "
                    +"(select group_concat(distinct name order by name separator ',' ) "
                    + "from (select name "
                    + "from stars s "
                    + "join stars_in_movies sim on s.id = sim.starId "
                    + "where sim.movieId = m.id "
                    + "limit 3) as limited_stars) " + "as stars, "
                    + "r.rating "
                    + "from movies m "
                    + "join ratings r on m.id = r.movieId "
                    + "order by r.rating desc "
                    + "limit 20;";


            // Declare statement
            try (PreparedStatement statement = conn.prepareStatement(query)){

                // Perform the query
                try (ResultSet rs = statement.executeQuery()) {
                    //create array to hold the jsonObjects
                    JsonArray jsonArray = new JsonArray();

                    // Iterate through each row of the result set
                    while (rs.next()) {
                        JsonObject jsonObject = new JsonObject();
                        // Get info
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");
                        String movieYear = rs.getString("year");
                        String movieDirector = rs.getString("director");
                        String movieRating = rs.getString("rating");
                        String movieGenres = rs.getString("genres");
                        //String movieStarsId = rs.getString("Id");
                        String movieStarsName = rs.getString("stars");

                        //place the info into the json object
                        jsonObject.addProperty("movies_id", movieId);
                        jsonObject.addProperty("movie_title", movieTitle);
                        jsonObject.addProperty("movie_year", movieYear);
                        jsonObject.addProperty("movie_director", movieDirector);
                        jsonObject.addProperty("movie_rating", movieRating);
                        jsonObject.addProperty("movie_genres", movieGenres);
                        //jsonObject.addProperty("movie_stars_id", movieStarsId);
                        jsonObject.addProperty("movie_stars", movieStarsName);

                        jsonArray.add(jsonObject);
                    }

                    // Write JSON string to output
                    out.write(jsonArray.toString());
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