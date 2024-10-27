import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

@WebServlet(name = "SearchPageServlet", urlPatterns = "/api/search-page")
public class SearchPageServlet extends HttpServlet {

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
        String title = request.getParameter("title_entry");
        String year = request.getParameter("year_entry");
        String director = request.getParameter("director_entry");
        String star = request.getParameter("star_entry");
        // limit; how many movies will be displayed on each page
        String display = request.getParameter("display");
        // offset; page 1 = offset 0
        String pageNumber = request.getParameter("pagenumber");


        response.setContentType("application/json");
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()){
/*
            String query = "select m.id, m.title , m.year, m.director, "

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
                    + "join ratings r on m.id = r.movieId " +
                    "order by r.rating desc limit ? offset ?;";

 */

            String query = "select m.id, m.title , m.year, m.director, "

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
                    + "join ratings r on m.id = r.movieId " +
                    "inner join stars_in_movies sim ON sim.movieId = m.id " +
                    "inner join stars s ON sim.starId = s.id where 1=1";
            if(title != null && !title.isEmpty()){
                query += " and m. title like ?";
            }
            if(year != null && !year.isEmpty()){
                query += " and m.year = ?";
            }
            if(director != null  && !director.isEmpty() ){
                query += " and m.director like ?";
            }
            if(star != null && !star.isEmpty()){
                query += " and s.name like ?";
            }

            query += " group by m.id, m.title, m.year, m.director, r.rating order by r.rating desc limit ? offset ?;";



            // Declare our statement
            try( PreparedStatement statement = conn.prepareStatement(query)) {

                // Set the parameter represented by "?" in the query to the id we get from url,
                // num 1 indicates the first "?" in the query
                int i = 1;

                if(title != null && !title.isEmpty()){
                    statement.setString(i++, "%" + title + "%");
                }
                if(year != null){
                    statement.setInt(i++, Integer.parseInt(year));
                }
                if(director != null && !director.isEmpty()){
                    statement.setString(i++, "%" + director + "%");
                }
                if(star != null && !star.isEmpty()){
                    statement.setString(i++, "%" + star + "%");
                }

                int limit = Integer.parseInt(display);
                statement.setInt(i++, limit);
                int offset = (Integer.parseInt(pageNumber) - 1) * limit;
                statement.setInt(i, offset);

                // Perform the query
                try (ResultSet rs = statement.executeQuery()) {
                    JsonArray array = new JsonArray();

                    // Create a json array for movies this star has acted in

                    // Iterate through each row of rs
                    while (rs.next()) {
                        JsonObject jsonObject = new JsonObject();
                        JsonObject movieGenres = new JsonObject();
                        // Get info
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");
                        String movieYear = rs.getString("year");
                        String movieDirector = rs.getString("director");
                        String movieRating = rs.getString("rating");

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

                        jsonObject.addProperty("movie_star1", movieStarName1);
                        jsonObject.addProperty("movie_star2", movieStarName2);
                        jsonObject.addProperty("movie_star3", movieStarName3);
                        jsonObject.addProperty("movie_star1_id", movieStarId1);
                        jsonObject.addProperty("movie_star2_id", movieStarId2);
                        jsonObject.addProperty("movie_star3_id", movieStarId3);

                        jsonObject.add("genres", movieGenres);

                        array.add(jsonObject);


                    }
                    // Write JSON string to output
                    out.write(array.toString());
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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }

    }
}
