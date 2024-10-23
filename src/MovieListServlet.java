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

        String genreId = request.getParameter("gid");
        // Retrieve parameter id from the url
        // Log message can be found in localhost log
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from the database
        try (Connection conn = dataSource.getConnection()) {
            // Construct query

            String selectQuery = "select m.id, m.title , m.year, m.director, "

                    //Selecting the first genre name
                    + "(select g.name "
                    + "from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + "where gim.movieId = m.id "
                    + "limit 1 offset 0) as genre1, "

                    //Selecting the second genre name
                    + "(select g.name "
                    + "from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + "where gim.movieId = m.id "
                    + "limit 1 offset 1) as genre2, "

                    //Selecting the third genre name
                    + "(select g.name "
                    + "from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + "where gim.movieId = m.id "
                    + "limit 1 offset 2) as genre3, "

                    //Selecting the first genre id
                    + "(select g.id "
                    + "from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + "where gim.movieId = m.id "
                    + "limit 1 offset 0) as genre1Id, "

                    //Selecting the second genre id
                    + "(select g.id "
                    + "from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + "where gim.movieId = m.id "
                    + "limit 1 offset 1) as genre2Id, "

                    //Selecting the third genre id
                    + "(select g.id "
                    + "from genres g "
                    + "join genres_in_movies gim on g.id = gim.genreId "
                    + "where gim.movieId = m.id "
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
                    + "join ratings r on m.id = r.movieId ";
                    String searchQuery = "";
                    String orderQuery = "order by r.rating desc ";
                    String limitQuery = "limit 20;";

                    if (genreId != null) {
                        searchQuery = "inner join genres_in_movies gim on m.id = gim.movieId " +
                                "inner join genres g on gim.genreId = g.id where g.id = ? ";
                    }

                    String query = selectQuery + searchQuery + orderQuery + limitQuery;

                    System.out.println(query);
           
            // Declare statement
            try (PreparedStatement statement = conn.prepareStatement(query)){
                if (genreId != null) {
                    statement.setInt(1, Integer.parseInt(genreId));
                }

                // Perform the query
                try (ResultSet rs = statement.executeQuery()) {
                    //create array to hold the jsonObjects
                    JsonArray jsonArray = new JsonArray();

                    // Iterate through each row of the result set
                    while (rs.next()) {
                        JsonObject jsonObject = new JsonObject();
                        JsonObject movieGenres = new JsonObject();
                        // Get info
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");
                        String movieYear = rs.getString("year");
                        String movieDirector = rs.getString("director");
                        String movieRating = rs.getString("rating");
                        //String movieGenres = rs.getString("genres");
                        //String movieStarsId = rs.getString("Id");
                        String movieStarName1 = rs.getString("star1");
                        String movieStarName2 = rs.getString("star2");
                        String movieStarName3 = rs.getString("star3");
                        String movieStarId1 = rs.getString("star1Id");
                        String movieStarId2 = rs.getString("star2Id");
                        String movieStarId3 = rs.getString("star3Id");

                        String movieGenreName1 = rs.getString("genre1");
                        String movieGenreName2 = rs.getString("genre2");
                        String movieGenreName3 = rs.getString("genre3");

                        String movieGenreId1 = rs.getString("genre1Id");
                        if (!rs.wasNull()) {
                            movieGenres.addProperty(movieGenreId1, movieGenreName1);
                        }
                        String movieGenreId2 = rs.getString("genre2Id");
                        if (!rs.wasNull()) {
                            movieGenres.addProperty(movieGenreId2, movieGenreName2);
                        }
                        String movieGenreId3 = rs.getString("genre3Id");
                        if (!rs.wasNull()) {
                            movieGenres.addProperty(movieGenreId3, movieGenreName3);
                        }

                        //place the info into the json object
                        jsonObject.addProperty("movie_id", movieId);
                        jsonObject.addProperty("movie_title", movieTitle);
                        jsonObject.addProperty("movie_year", movieYear);
                        jsonObject.addProperty("movie_director", movieDirector);
                        jsonObject.addProperty("movie_rating", movieRating);
                        //jsonObject.addProperty("movie_genres", movieGenres);
                        //jsonObject.addProperty("movie_stars_id", movieStarsId);
                        jsonObject.addProperty("movie_star1", movieStarName1);
                        jsonObject.addProperty("movie_star2", movieStarName2);
                        jsonObject.addProperty("movie_star3", movieStarName3);
                        jsonObject.addProperty("movie_star1_id", movieStarId1);
                        jsonObject.addProperty("movie_star2_id", movieStarId2);
                        jsonObject.addProperty("movie_star3_id", movieStarId3);

//                        jsonObject.addProperty("movie_genre_name1", movieGenreName1);
//                        jsonObject.addProperty("movie_genre_name2", movieGenreName2);
//                        jsonObject.addProperty("movie_genre_name3", movieGenreName3);
//                        jsonObject.addProperty("movie_genre_id1", movieGenreId1);
//                        jsonObject.addProperty("movie_genre_id2", movieGenreId2);
//                        jsonObject.addProperty("movie_genre_id3", movieGenreId3);
                        jsonObject.add("genres", movieGenres);

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