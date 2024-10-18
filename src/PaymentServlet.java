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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

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

        String firstName = request.getParameter("fname");
        String lastName = request.getParameter("lname");
        String creditCardNumber = request.getParameter("ccn");
        Date expirationDate = Date.valueOf(request.getParameter("expdate"));

        PrintWriter out = response.getWriter();

        String query = "select * from creditcards where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, creditCardNumber);

            boolean success;

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String cardFirstName = rs.getString("firstName");
                    String cardLastName = rs.getString("lastName");
                    Date cardExpirationDate = rs.getDate("expiration");

                    success = cardFirstName.equals(firstName)
                            && cardLastName.equals(lastName)
                            && cardExpirationDate.equals(expirationDate);
                } else {
                    success = false;
                }
            }

            String status = success ? "success" : "fail";
            String message = success ? "Payment successful." : "Payment failed. Please try again.";

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", status);
            responseJsonObject.addProperty("message", message);

            if (!success) request.getServletContext().log("Login failed");

            out.write(responseJsonObject.toString());
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

}
