import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
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
        // Retrieve parameter id from url request.
        String id = request.getParameter("id");
        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Construct a query with parameter represented by "?"
            String query = "select * from stars as s, stars_in_movies as sim, movies as m " +
                    "where m.id = sim.movieId and sim.starId = s.id and s.id = ?";

            // Declare our statement
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                // Set the parameter represented by "?" in the query to the id we get from url,
                // num 1 indicates the first "?" in the query
                statement.setString(1, id);

                // Perform the query
                try (ResultSet rs = statement.executeQuery()) {
                    JsonArray jsonArray = new JsonArray();

                    // Iterate through each row of rs
                    while (rs.next()) {
                        // Star info
                        String starId = rs.getString("starId");
                        String starName = rs.getString("name");
                        String birthYear = rs.getString("birthYear");
                        String starDob = rs.wasNull() ? "N/A" : birthYear;

                        // Create a json array for movies this star has acted in
                        JsonArray starMovies = new JsonArray();
                        do {
                            // If this result set contains info for another star, stop looping
                            if (!rs.getString("starId").equals(starId)) {
                                // Rollback the cursor to the previous star to prepare for the next loop
                                rs.previous();
                                break;
                            }
                            // Extract info for each movie
                            JsonObject movieData = new JsonObject();
                            String movieId = rs.getString("movieId");
                            String movieTitle = rs.getString("title");
                            String movieYear = rs.getString("year");
                            String movieDirector = rs.getString("director");

                            movieData.addProperty("movie_id", movieId);
                            movieData.addProperty("movie_title", movieTitle);
                            movieData.addProperty("movie_year", movieYear);
                            movieData.addProperty("movie_director", movieDirector);

                            // Add each movie to the json array of movies
                            starMovies.add(movieData);
                        } while (rs.next());

                        // Create a JsonObject based on the data we retrieve from rs
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("star_id", starId);
                        jsonObject.addProperty("star_name", starName);
                        jsonObject.addProperty("star_dob", starDob);
                        jsonObject.add("star_movies", starMovies);

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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
