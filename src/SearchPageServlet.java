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
    private static final String selectMoviesQuery = "select m.id, m.title , m.year, m.director, " +
            //Selecting the first genre name
            "(select g.name from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "limit 1 offset 0) as genre1, " +
            //Selecting the second genre name
            "(select g.name from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "limit 1 offset 1) as genre2, " +
            //Selecting the third genre name
            "(select g.name from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "limit 1 offset 2) as genre3, "
            //Selecting the first genre id
            + "(select g.id from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "limit 1 offset 0) as genre1Id, " +
            //Selecting the second genre id
            "(select g.id from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "limit 1 offset 1) as genre2Id, " +
            //Selecting the third genre id
            "(select g.id from genres g " +
            "join genres_in_movies gim on g.id = gim.genreId " +
            "where gim.movieId = m.id " +
            "limit 1 offset 2) as genre3Id, " +
            // getting star1
            "(select s.name from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 0) as star1, " +
            // getting star1 id
            "(select s.id from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 0) as star1Id, " +
            //star 2
            "(select s.name from stars s " +
            "join stars_in_movies sim on s.id = sim.starId " +
            "where sim.movieId = m.id " +
            "limit 1 offset 1) as star2, " +
            //star2 id
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
            "limit 1 offset 2) as star3Id, " +
            "r.rating " +
            "from movies m " +
            "left join ratings r on m.id = r.movieId " +
            "inner join stars_in_movies sim ON sim.movieId = m.id " +
            "inner join stars s ON sim.starId = s.id " +
            // Fulltext search
            "where match (title) against (? in boolean mode) " +
            "group by m.id, m.title, m.year, m.director, r.rating " +
            // Can order by (rating asc/desc, title asc/desc) or (title asc/desc, rating asc/desc)
            "order by ?, ? " +
            "limit ? offset ?";

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

        String searchQuery = request.getParameter("search-query");
        if (searchQuery.isBlank()) {
            JsonObject emptyJson = new JsonObject();
            response.getWriter().write(emptyJson.toString());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String ratingOrder = "rating desc";
        String titleOrder = "title asc";
        String display = request.getParameter("display");
        int limit = Integer.parseInt(display);
        String pageNumber = request.getParameter("page-number");
        int offset = (Integer.parseInt(pageNumber) - 1) * limit;

        String booleanModeSearchQuery = Stream.of(searchQuery.split(" "))
                .map(word -> "+" + word + "*")
                .collect(Collectors.joining(" "));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectMoviesQuery)) {
            preparedStatement.setString(1, booleanModeSearchQuery);
            preparedStatement.setString(2, ratingOrder);
            preparedStatement.setString(3, titleOrder);
            preparedStatement.setInt(4, limit);
            preparedStatement.setInt(5, offset);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JsonArray jsonArray = new JsonArray();

                while (resultSet.next()) {
                    JsonObject jsonObject = new JsonObject();
                    JsonObject movieGenresJsonObject = new JsonObject();
                    JsonObject movieStarsJsonObject = new JsonObject();

                    String movieId = resultSet.getString("id");
                    String movieTitle = resultSet.getString("title");
                    String movieYear = resultSet.getString("year");
                    String movieDirector = resultSet.getString("director");
                    String movieRating = resultSet.getString("rating");
                    if (resultSet.wasNull()) {
                        movieRating = "None";
                    }

                    addNonNullValueToJsonObject(resultSet, "star1", movieStarsJsonObject);
                    addNonNullValueToJsonObject(resultSet, "star2", movieStarsJsonObject);
                    addNonNullValueToJsonObject(resultSet, "star3", movieStarsJsonObject);

                    addNonNullValueToJsonObject(resultSet, "genre1", movieGenresJsonObject);
                    addNonNullValueToJsonObject(resultSet, "genre2", movieGenresJsonObject);
                    addNonNullValueToJsonObject(resultSet, "genre3", movieGenresJsonObject);

                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_year", movieYear);
                    jsonObject.addProperty("movie_director", movieDirector);
                    jsonObject.addProperty("movie_rating", movieRating);

                    jsonObject.add("stars", movieStarsJsonObject);
                    jsonObject.add("genres", movieGenresJsonObject);

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

    private void addNonNullValueToJsonObject(ResultSet resultSet, String columnName, JsonObject jsonObject) throws SQLException {
        String id = resultSet.getString(columnName + "Id");
        if (!resultSet.wasNull()) {
            String name = resultSet.getString(columnName);
            jsonObject.addProperty(id, name);
        }
    }
}
