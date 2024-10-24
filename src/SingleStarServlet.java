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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
        // Retrieve parameter id from url request.
        String id = request.getParameter("id");
        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Construct a query with parameter represented by "?"
        String query = "select * " +
                "from ((stars as s " +
                "inner join stars_in_movies as sim on s.id = sim.starId) " +
                "inner join movies as m on sim.movieId = m.id) " +
                "where s.id = ? " +
                "order by m.year desc, m.title";

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection();
             // Declare our statement
             PreparedStatement statement = conn.prepareStatement(query)) {

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            try (ResultSet rs = statement.executeQuery()) {
                JsonObject starJsonObject = new JsonObject();

                // Create a json array for movies this star has acted in
                JsonArray starMoviesJsonArray = new JsonArray();

                // Iterate through each row of rs
                while (rs.next()) {
                    // Star info
                    String starId = rs.getString("starId");
                    String starName = rs.getString("name");
                    String birthYear = rs.getString("birthYear");
                    String starDob = rs.wasNull() ? "N/A" : birthYear;

                    // Extract info for each movie
                    String movieId = rs.getString("movieId");
                    String movieTitle = rs.getString("title");
                    String movieYear = rs.getString("year");
                    String movieDirector = rs.getString("director");

                    // Add info to star json object
                    starJsonObject.addProperty("star_id", starId);
                    starJsonObject.addProperty("star_name", starName);
                    starJsonObject.addProperty("star_dob", starDob);

                    // Create a json object for each movie
                    JsonObject movieJsonObject = new JsonObject();
                    movieJsonObject.addProperty("movie_id", movieId);
                    movieJsonObject.addProperty("movie_title", movieTitle);
                    movieJsonObject.addProperty("movie_year", movieYear);
                    movieJsonObject.addProperty("movie_director", movieDirector);

                    // Add each movie to the json array of movies
                    starMoviesJsonArray.add(movieJsonObject);
                }

                starJsonObject.add("star_movies", starMoviesJsonArray);

                // Write JSON string to output
                out.write(starJsonObject.toString());
                // Set response status to 200 (OK)
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
