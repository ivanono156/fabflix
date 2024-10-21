import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

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

            boolean success = false;

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String cardFirstName = rs.getString("firstName");
                    String cardLastName = rs.getString("lastName");
                    Date cardExpirationDate = rs.getDate("expiration");

                    success = cardFirstName.equals(firstName)
                            && cardLastName.equals(lastName)
                            && cardExpirationDate.equals(expirationDate);
                }
            }

            int updatedRows = 0;
            if (success) {
                // Get shopping cart from session
                HttpSession session = request.getSession();
                ArrayList<ShoppingCartServlet.CartItem> cart =
                        (ArrayList<ShoppingCartServlet.CartItem>) session.getAttribute(ShoppingCartServlet.shoppingCartAttributeName);

                String insertQuery = "insert into sales (customerId, movieId, saleDate) " +
                        "select c.id, ?, ?" +
                        "from customers as c inner join creditcards as cc on c.ccid = cc.id " +
                        "where cc.id = ?";

                try (PreparedStatement insertStatement = conn.prepareStatement(insertQuery)) {
                    for (ShoppingCartServlet.CartItem item : cart) {
                        // Add sale to database
                        String movieId = item.getMovieId();
                        // FIXME: might wanna make a column in sales table that holds the quantity of each movie/sale?
                        Date saleDate = Date.valueOf(LocalDate.now());

                        insertStatement.setString(1, movieId);
                        insertStatement.setDate(2, saleDate);
                        insertStatement.setString(3, creditCardNumber);
                        updatedRows += insertStatement.executeUpdate();
                    }
                }

                // Clear the cart after purchase is completed
                cart.clear();
            }

            String status = success ? "success" : "fail";
            String message = success ? "Payment successful." : "Payment failed. Please try again.";

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", status);
            responseJsonObject.addProperty("message", message);
            responseJsonObject.addProperty("updated_rows", updatedRows);

            if (!success) request.getServletContext().log("Payment failed");

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
