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
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(name = "SearchPageServlet", urlPatterns = "/api/search-page")
public class SearchPageServlet extends HttpServlet {
    private static final String selectMoviesQuery = "select m.id, m.title , m.year, m.director, r.rating, " +
            // Selecting the first genre name
            "(select g.name from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "order by g.name " +
            "limit 1 offset 0) as genre1, " +
            // Selecting the second genre name
            "(select g.name from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "order by g.name " +
            "limit 1 offset 1) as genre2, " +
            // Selecting the third genre name
            "(select g.name from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "order by g.name " +
            "limit 1 offset 2) as genre3, " +
            // Selecting the first genre id
            "(select g.id from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "order by g.name " +
            "limit 1 offset 0) as genre1Id, " +
            // Selecting the second genre id
            "(select g.id from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "order by g.name " +
            "limit 1 offset 1) as genre2Id, " +
            // Selecting the third genre id
            "(select g.id from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "order by g.name " +
            "limit 1 offset 2) as genre3Id, " +
            // star1
            "(select s.name from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 0) as star1, " +
            // star1 id
            "(select s.id from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 0) as star1Id, " +
            // star 2
            "(select s.name from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 1) as star2, " +
            // star2 id
            "(select s.id from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 1) as star2Id, " +
            // star 3
            "(select s.name from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 2) as star3, " +
            // star3 id
            "(select s.id from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 2) as star3Id " +
            "from movies m " +
            "left join ratings r on m.id = r.movieId " +
            "inner join stars_in_movies sim ON sim.movieId = m.id " +
            "inner join stars s ON sim.starId = s.id " +
            // Fulltext search
            "where match (title) against (? in boolean mode) " +    //or edth(?, title, ?) " +  FUZZY SEARCH
            "group by m.id, m.title, m.year, m.director, r.rating " +
            // Can order by (rating asc/desc, title asc/desc) or (title asc/desc, rating asc/desc)
            "order by %s " +
            "limit ? offset ?";

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String searchQuery = request.getParameter("search-query");
        if (searchQuery.isBlank()) {
            JsonObject emptyJson = new JsonObject();
            response.getWriter().write(emptyJson.toString());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String sortOrder = request.getParameter("sort-order");
        String display = request.getParameter("display");
        int limit = Integer.parseInt(display);
        String pageNumber = request.getParameter("page-number");
        int offset = (Integer.parseInt(pageNumber) - 1) * limit;

        String booleanModeSearchQuery = Stream.of(searchQuery.split(" "))
                .map(word -> "+" + word + "*")
                .collect(Collectors.joining(" "));

        int threshold = Math.max(1, (int) Math.floor(searchQuery.length() * editDistanceThreshold));

        String query = String.format(selectMoviesQuery, sortOrder);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, booleanModeSearchQuery);
            preparedStatement.setString(2, searchQuery);
//            preparedStatement.setInt(3, threshold);   FUZZY SEARCH
            preparedStatement.setInt(3, limit);
            preparedStatement.setInt(4, offset);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JsonArray jsonArray = new JsonArray();

                while (resultSet.next()) {
                    JsonObject jsonObject = new JsonObject();
                    JsonArray movieGenresJsonArray = new JsonArray();
                    JsonArray movieStarsJsonArray = new JsonArray();

                    String movieId = resultSet.getString("id");
                    String movieTitle = resultSet.getString("title");
                    String movieYear = resultSet.getString("year");
                    String movieDirector = resultSet.getString("director");
                    String movieRating = resultSet.getString("rating");
                    if (resultSet.wasNull()) {
                        movieRating = "None";
                    }

                    addNonNullValueToJsonObject(resultSet, "star1", movieStarsJsonArray);
                    addNonNullValueToJsonObject(resultSet, "star2", movieStarsJsonArray);
                    addNonNullValueToJsonObject(resultSet, "star3", movieStarsJsonArray);

                    addNonNullValueToJsonObject(resultSet, "genre1", movieGenresJsonArray);
                    addNonNullValueToJsonObject(resultSet, "genre2", movieGenresJsonArray);
                    addNonNullValueToJsonObject(resultSet, "genre3", movieGenresJsonArray);

                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_rating", movieRating);

                    jsonObject.add("stars", movieStarsJsonArray);
                    jsonObject.add("genres", movieGenresJsonArray);

                    jsonArray.add(jsonObject);
                }
                response.getWriter().write(jsonArray.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void addNonNullValueToJsonObject(ResultSet resultSet, String columnName, JsonArray jsonArray) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        String id = resultSet.getString(columnName + "Id");
        if (!resultSet.wasNull()) {
            String name = resultSet.getString(columnName);
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("name", name);
            if (!jsonArray.contains(jsonObject)) {
                jsonArray.add(jsonObject);
            }
        }
    }
}
