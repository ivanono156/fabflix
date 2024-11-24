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
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet { ;

    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup(JdbcConstants.readWriteDataSourceURL);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String movieTitle = request.getParameter("movie_title");
        String movieYear = request.getParameter("movie_year");
        String movieDirector = request.getParameter("movie_director");
        String starName = request.getParameter("star_name");
        String genreName = request.getParameter("movie_genre");

        try (Connection conn = dataSource.getConnection()) {
            CallableStatement statement = conn.prepareCall("{call add_movie(?,?,?,?,?,?,?,?,?)}");
            statement.setString(1, movieTitle);
            statement.setInt(2, Integer.parseInt(movieYear));
            statement.setString(3, movieDirector);
            statement.setString(4, starName);
            statement.setString(5, genreName);

            statement.registerOutParameter(6, Types.VARCHAR);
            statement.registerOutParameter(7, Types.VARCHAR);
            statement.registerOutParameter(8, Types.INTEGER);
            statement.registerOutParameter(9, Types.VARCHAR);

            statement.execute();

            String newMovieId = statement.getString(6);
            String newStarId = statement.getString(7);
            int newGenreId = statement.getInt(8);
            String statusMessage = statement.getString(9);

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("new_movie_id", newMovieId);
            responseJsonObject.addProperty("new_star_id", newStarId);
            responseJsonObject.addProperty("new_genre_id", newGenreId);
            responseJsonObject.addProperty("status_message", statusMessage);
            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}


