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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();

        String query = "select password from customers where email = ?";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);

            JsonObject responseJsonObject = new JsonObject();

            boolean success = false;
            String message;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Email account is found; retrieve its associated password
                    String foundPassword = rs.getString("password");

                    if (foundPassword.equals(password)) {
                        // Login success - Set this user into the session
                        request.getSession().setAttribute("user", new User(email));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                    } else {
                        // Login fail - password was incorrect

                        responseJsonObject.addProperty("status", "fail");
                        // Log to localhost log
                        request.getServletContext().log("Login failed");
                        responseJsonObject.addProperty("message", "incorrect password");
                    }

                } else {
                    // Login fail - email was not found
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");

                    responseJsonObject.addProperty("message", "email " + email + " doesn't exist");
                }
//
//                if (success) {
//                    // Login success
//                } else {
//                    // Login fail
//                    responseJsonObject.addProperty("status", "fail");
//                    // Log to localhost log
//                    request.getServletContext().log("Login failed");
//
//                    responseJsonObject.addProperty("message", message);
//                }
            }
            out.write(responseJsonObject.toString());
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
