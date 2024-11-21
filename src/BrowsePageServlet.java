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
@WebServlet(name = "BrowsePageServlet", urlPatterns = "/api/browse-page")
public class BrowsePageServlet extends HttpServlet {

    // Create a data source register in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup(JdbcConstants.readOnlyDataSourceURL);
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
            String genresQuery = "select g.id, g.name from genres g order by g.name;";

            //create array to hold the jsonObjects
            JsonArray genresJsonArray = new JsonArray();

            // Declare statement
            try (PreparedStatement statement = conn.prepareStatement(genresQuery)){
                // Perform the query
                try (ResultSet rs = statement.executeQuery()) {
                    // Iterate through each row of the result set
                    while (rs.next()) {
                        JsonObject jsonObject = new JsonObject();
                        String genreId = rs.getString("id");
                        String genreName = rs.getString("name");

                        jsonObject.addProperty("genre_id", genreId);
                        jsonObject.addProperty("genre_name", genreName);
                        genresJsonArray.add(jsonObject);
                    }
                }
            }

            String titlesQuery = "select distinct substring(title, 1, 1) as start_char from movies order by start_char";

            JsonArray titlesJsonArray = new JsonArray();

            try (PreparedStatement statement = conn.prepareStatement(titlesQuery);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String titleStartChar = rs.getString("start_char");
                    if (Character.isLetterOrDigit(titleStartChar.charAt(0))) {
                        // only return alphanumerical characters
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("title_id", titleStartChar);
                        jsonObject.addProperty("title_name", titleStartChar);

                        titlesJsonArray.add(jsonObject);
                    }
                }

                // Finally, add the special character "*"
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title_id", "non-alnum");
                jsonObject.addProperty("title_name", "*");

                titlesJsonArray.add(jsonObject);
            }

            JsonObject browseOptionsJsonObject = new JsonObject();

            browseOptionsJsonObject.add("genres", genresJsonArray);
            browseOptionsJsonObject.add("titles", titlesJsonArray);

            // Write JSON string to output
            out.write(browseOptionsJsonObject.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
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