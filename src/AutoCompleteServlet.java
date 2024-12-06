import com.google.gson.JsonArray;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    private static final String fullTextSearchQuery = "select id, title from movies " +
            "where match (title) against (? in boolean mode) " +    //or edth(?, title, ?) " + FUZZY SEARCH
            "limit 10";

    private static final double editDistanceThreshold = 0.25;

    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup(JdbcConstants.readOnlyDataSourceURL);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String searchQuery = request.getParameter("search-query");
        if (searchQuery.isBlank()) {
            JsonObject emptyJson = new JsonObject();
            response.getWriter().write(emptyJson.toString());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String booleanModeSearchQuery = Stream.of(searchQuery.split(" "))
                .map(word -> "+" + word + "*")
                .collect(Collectors.joining(" "));

        int threshold = Math.max(1, (int) Math.floor(searchQuery.length() * editDistanceThreshold));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(fullTextSearchQuery)) {
            preparedStatement.setString(1, booleanModeSearchQuery);
            preparedStatement.setString(2, searchQuery);
//            preparedStatement.setInt(3, threshold);

            JsonArray jsonArray = new JsonArray();

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String movieTitle = resultSet.getString("title");
                    String movieId = resultSet.getString("id");

                    jsonArray.add(generateJsonObject(movieId, movieTitle));
                }
            }

            response.getWriter().write(jsonArray.toString());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

	private static JsonObject generateJsonObject(String movieId, String movieTitle) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", movieTitle);

		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movie_id", movieId);

		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}
}
