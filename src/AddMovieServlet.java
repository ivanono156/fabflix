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
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet { ;

    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String movie_title = request.getParameter("movie_title");
        String movie_year = request.getParameter("movie_year");
        String movie_director = request.getParameter("movie_director");
        String movie_star = request.getParameter("star_name");
        String movie_genre = request.getParameter("movie_genre");

        JsonObject jsonResponse = new JsonObject();
        String result;
        try (Connection conn = dataSource.getConnection()){
            CallableStatement statement = conn.prepareCall("{call add_movie(?,?,?,?,?,?,?,?,?)}");
            statement.setString(1, movie_title);
            statement.setInt(2, Integer.parseInt(movie_year));
            statement.setString(3, movie_director);
            statement.setString(4, movie_star);
            statement.setString(5, movie_genre);

            statement.registerOutParameter(6, java.sql.Types.VARCHAR);
            statement.registerOutParameter(7, java.sql.Types.VARCHAR);
            statement.registerOutParameter(8, Types.INTEGER);
            statement.registerOutParameter(9, Types.VARCHAR);
            System.out.println("About to execute statement");

            statement.execute();

            String new_movie_id = statement.getString(6);
            String new_star_id = statement.getString(7);
            int new_genre_id = statement.getInt(8);
            System.out.println(new_genre_id);
            String statusMessage = statement.getString(9);
            //jsonResponse.addProperty("new_movie_id", new_movie_id);
            System.out.print("Added movie");
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("new_movie_id", new_movie_id);
            responseJsonObject.addProperty("new_star_id", new_star_id);
            responseJsonObject.addProperty("new_genre_id", new_genre_id);
            responseJsonObject.addProperty("status_message", statusMessage);
            //responseJsonObject.addProperty("message", "Added movie successfully");
            out.write(responseJsonObject.toString());
            response.setStatus(HttpServletResponse.SC_OK);
        }catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {

            out.close();
        }
    }
}


