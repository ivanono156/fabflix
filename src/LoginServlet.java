import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;
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
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    private static final String credentialsQuery = "select * from customers where email = ?";

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        try {
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject recaptchaErrorJson = getResponseJsonObject(false, e.getMessage());
            response.getWriter().write(recaptchaErrorJson.toString());
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(credentialsQuery)) {

            ps.setString(1, email);

            boolean success = false;
            String message;

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Email account is found; retrieve its associated password
                    String encryptedFoundPassword = rs.getString("password");
                    System.out.println("Encrypted pass is : " + encryptedFoundPassword);
                    int id = rs.getInt("id");

                    // use the same encryptor to compare the user input password with encrypted password stored in DB
                    success = new StrongPasswordEncryptor().checkPassword(password, encryptedFoundPassword);
                    if (success) {
                        message = "success";
                        // Set this user into the session
                        request.getSession().setAttribute("user", new User(id));
                    } else {
                        message = "Incorrect password";
                    }
                } else {
                    message = "Email " + email + " not found";
                }

                if (!success) {
                    request.getServletContext().log("Login failed");
                }

                JsonObject responseJsonObject = getResponseJsonObject(success, message);
                response.getWriter().write(responseJsonObject.toString());
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private JsonObject getResponseJsonObject(boolean success, String message) {
        JsonObject jsonObject = new JsonObject();
        String status = success ? "success" : "fail";
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("message", message);
        return jsonObject;
    }
}
