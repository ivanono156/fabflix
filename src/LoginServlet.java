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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();

        String query = "select * from customers where email = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);

            JsonObject responseJsonObject = new JsonObject();

            boolean success;
            String message;

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Email account is found; retrieve its associated password
                    String foundPassword = rs.getString("password");
                    int id = rs.getInt("id");

                    if (foundPassword.equals(password)) {
                        success = true;
                        message = "success";
                        // Set this user into the session
                        request.getSession().setAttribute("user", new User(email, id));
                    } else {
                        success = false;
                        message = "incorrect password";
                    }
                } else {
                    success = false;
                    message = "email " + email + " not found";
                }

                if (success) {
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", message);
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", message);
                    request.getServletContext().log("Login failed");
                }
            }

            out.write(responseJsonObject.toString());
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
