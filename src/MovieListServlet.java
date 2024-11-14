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
import java.sql.SQLException;
import java.util.ArrayList;

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

        String genreId = request.getParameter("gid");

        String titleStartsWith = request.getParameter("title-starts-with");

        // Pagination params
        // limit; how many movies will be displayed on each page
        String display = request.getParameter("display");
        // offset; page 1 = offset 0
        String pageNumber = request.getParameter("page-number");
        String sortFieldEntry = request.getParameter("sort_field");
        String sortOrderEntry = request.getParameter("sort_order");
        String sortFieldEntry2 = request.getParameter("sort_field2");
        String sortOrderEntry2 = request.getParameter("sort_order2");
        if (sortFieldEntry == null) {
            sortFieldEntry = "rating"; // Default to sorting by rating
        }
        if (sortOrderEntry == null || (!sortOrderEntry.equalsIgnoreCase("ASC") && !sortOrderEntry.equalsIgnoreCase("DESC"))) {
            sortOrderEntry = "DESC"; // Default to descending order
        }

        // Retrieve parameter id from the url
        // Log message can be found in localhost log
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from the database
        try (Connection conn = dataSource.getConnection()) {
            // Construct query

            String selectQuery = "select distinct m.id, m.title , m.year, m.director, "

                //Selecting the first genre name
                + "(select g.name "
                + "from genres g "
                + "join genres_in_movies gim on g.id = gim.genreId "
                + "where gim.movieId = m.id " + "order by g.name "
                + "limit 1 offset 0) as genre1, "

                //Selecting the second genre name
                + "(select g.name "
                + "from genres g "
                + "join genres_in_movies gim on g.id = gim.genreId "
                + "where gim.movieId = m.id " + "order by g.name "
                + "limit 1 offset 1) as genre2, "

                //Selecting the third genre name
                + "(select g.name "
                + "from genres g "
                + "join genres_in_movies gim on g.id = gim.genreId "
                + "where gim.movieId = m.id " + "order by g.name "
                + "limit 1 offset 2) as genre3, "

                //Selecting the first genre id
                + "(select g.id "
                + "from genres g "
                + "join genres_in_movies gim on g.id = gim.genreId "
                + "where gim.movieId = m.id " + "order by g.name "
                + "limit 1 offset 0) as genre1Id, "

                //Selecting the second genre id
                + "(select g.id "
                + "from genres g "
                + "join genres_in_movies gim on g.id = gim.genreId "
                + "where gim.movieId = m.id " + "order by g.name "
                + "limit 1 offset 1) as genre2Id, "

                //Selecting the third genre id
                + "(select g.id "
                + "from genres g "
                + "join genres_in_movies gim on g.id = gim.genreId "
                + "where gim.movieId = m.id " + "order by g.name "
                + "limit 1 offset 2) as genre3Id, "


                +"(select s.name " // getting star1
                + "from stars s "
                + "join stars_in_movies sim on s.id = sim.starId "
                + "where sim.movieId = m.id "
                + "limit 1 offset 0) as star1, "

                +"(select s.id " // getting star1 id
                + "from stars s "
                + "join stars_in_movies sim on s.id = sim.starId "
                + "where sim.movieId = m.id "
                + "limit 1 offset 0) as star1Id, "

                +"(select s.name " //star 2
                + "from stars s "
                + "join stars_in_movies sim on s.id = sim.starId "
                + "where sim.movieId = m.id "
                + "limit 1 offset 1) as star2, "

                +"(select s.id " //star2 id
                + "from stars s "
                + "join stars_in_movies sim on s.id = sim.starId "
                + "where sim.movieId = m.id "
                + "limit 1 offset 1) as star2Id, "

                +"(select s.name " // star 3
                + "from stars s "
                + "join stars_in_movies sim on s.id = sim.starId "
                + "where sim.movieId = m.id "
                + "limit 1 offset 2) as star3, "

                +"(select s.id " // star3 id
                + "from stars s "
                + "join stars_in_movies sim on s.id = sim.starId "
                + "where sim.movieId = m.id "
                + "limit 1 offset 2) as star3Id, "

                + "r.rating "
                + "from movies m "
                + "left join ratings r on m.id = r.movieId ";

            String searchQuery = "";

            String limitQuery = "limit ? offset ?;";

            String orderQuery = "order by ";
            if (sortFieldEntry != null && sortFieldEntry2!= null && sortOrderEntry != null && sortOrderEntry2 != null) {
                if(sortFieldEntry.equalsIgnoreCase("title")){
                    orderQuery += "m.title " + sortOrderEntry + ", r.rating " + sortOrderEntry2 + " ";
                }
                else{
                    orderQuery += "r.rating " + sortFieldEntry + ", m.title " + sortOrderEntry2 + " ";
                }
            }
            else{
                orderQuery += "r.rating desc ";
            }

            if (genreId != null) {
                searchQuery = "inner join genres_in_movies gim on m.id = gim.movieId " +
                        "inner join genres g on gim.genreId = g.id where g.id = ? ";
            } else if (titleStartsWith != null) {
                if (titleStartsWith.equalsIgnoreCase("non-alnum")) {
                    searchQuery = "where substring(m.title, 1, 1) REGEXP '[^a-z0-9]+' ";
                } else {
                    searchQuery = "where m.title like ? ";
                }
            }

            String query = selectQuery + searchQuery + orderQuery + limitQuery;
           
            // Declare statement
            try (PreparedStatement statement = conn.prepareStatement(query)){
                int params = 1;
                if (genreId != null) {
                    statement.setInt(params++, Integer.parseInt(genreId));
                } else if (titleStartsWith != null) {
                    if (!titleStartsWith.equalsIgnoreCase("non-alnum")) {
                        statement.setString(params++, titleStartsWith + "%");
                    }
                }

                // Set the limit & offset params for pagination
                int limit = Integer.parseInt(display);
                statement.setInt(params++, limit);
                int offset = (Integer.parseInt(pageNumber) - 1) * limit;
                statement.setInt(params, offset);

                // Perform the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    //create array to hold the jsonObjects
                    JsonArray jsonArray = new JsonArray();

                    // Iterate through each row of the result set
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