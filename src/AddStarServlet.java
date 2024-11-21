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

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {
    private static final String insertStarQuery = "insert into stars (id, name, birthYear) values (?, ?, ?)";
    private static final String getMaxStarIdQuery = "select max(id) from stars";

    private DataSource dataSource;

    static class SQLUpdateStatus {
        private final boolean success;
        private final String message;
        private final int updatedRows;

        public SQLUpdateStatus(boolean success, String message, int updatedRows) {
            this.success = success;
            this.message = message;
            this.updatedRows = updatedRows;
        }

        public boolean updateWasSuccessful() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getUpdatedRows() {
            return updatedRows;
        }
    }

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
        try (Connection connection = dataSource.getConnection()) {
            String starId = getNewStarId(connection);
            String starName = request.getParameter("star-name");
            String birthYear = request.getParameter("star-birth-year");

            SQLUpdateStatus updateStatus;
            if (starName.isBlank()) {
                updateStatus = new SQLUpdateStatus(false, "Name is blank", 0);
            } else {
                updateStatus = addNewStarToDataBase(connection, starId, starName, birthYear);
            }

            JsonObject responseJsonObject = new JsonObject();
            String status = updateStatus.updateWasSuccessful() ? "success" : "failure";
            responseJsonObject.addProperty("status", status);
            responseJsonObject.addProperty("message", updateStatus.getMessage());
            responseJsonObject.addProperty("updated_rows", updateStatus.getUpdatedRows());
            if (updateStatus.updateWasSuccessful()) {
                responseJsonObject.addProperty("generated_star_id", starId);
            }

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

    private String getNewStarId(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getMaxStarIdQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String maxStarId = resultSet.getString("max(id)");
                String starIdStrPrefix = maxStarId.replaceAll("\\d+", "");
                int starIdIntSuffix = Integer.parseInt(maxStarId.replaceAll("\\D+", ""));
                return starIdStrPrefix + (starIdIntSuffix + 1);
            }
            return "";
        }
    }

    private SQLUpdateStatus addNewStarToDataBase(Connection connection, String starId, String starName, String birthYear) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertStarQuery)) {
            preparedStatement.setString(1, starId);
            preparedStatement.setString(2, starName);
            if (birthYear.isBlank()) {
                preparedStatement.setNull(3, Types.INTEGER);
            } else {
                int birthYearInt = Integer.parseInt(birthYear);
                preparedStatement.setInt(3, birthYearInt);
            }

            int updatedRows =  preparedStatement.executeUpdate();
            return new SQLUpdateStatus(true, "success", updatedRows);
        } catch (NumberFormatException nfe) {
            return new SQLUpdateStatus(false, "Invalid birth year value", 0);
        }
    }
}
