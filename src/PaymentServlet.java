import com.google.gson.JsonArray;
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
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

    private DataSource dataSource;

    public static String salesIdsAttributeName = "sales";

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

        String firstName = request.getParameter("fname");
        String lastName = request.getParameter("lname");
        String creditCardNumber = request.getParameter("ccn");
        Date expirationDate = Date.valueOf(request.getParameter("expdate"));

        PrintWriter out = response.getWriter();

        String query = "select * from creditcards where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

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

            JsonObject responseJsonObject = new JsonObject();

            // Get shopping cart from session
            HttpSession session = request.getSession();
            ArrayList<ShoppingCartServlet.CartItem> cart =
                    (ArrayList<ShoppingCartServlet.CartItem>) session.getAttribute(ShoppingCartServlet.shoppingCartAttributeName);

            if (cart == null) { success = false; }

            if (success) {

                ArrayList<String> saleIds = addSalesToDatabase(conn, ((User)session.getAttribute("user")).getId(), cart);
                // For retrieving the sales made in this transaction in the order confirmation page
                session.setAttribute(salesIdsAttributeName, saleIds);

                JsonArray salesJsonArray = createSalesJsonArray(saleIds);
                responseJsonObject.add("sales", salesJsonArray);
            }

            String status = success ? "success" : "fail";
            String message = success ? "Payment successful." : "Payment failed. Please try again.";

            responseJsonObject.addProperty("status", status);
            responseJsonObject.addProperty("message", message);

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

    private ArrayList<String> addSalesToDatabase(Connection conn, int customerId, ArrayList<ShoppingCartServlet.CartItem> cart) throws SQLException {

        String insertQuery = "insert into sales (customerId, movieId, saleDate, quantity) values (?, ?, ?, ?)";

        ArrayList<String> salesIdsArray = new ArrayList<>();

        try (PreparedStatement insertStatement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            // Add each movie to the database (each movie = 1 sale)
            for (ShoppingCartServlet.CartItem item : cart) {
                String movieId = item.getMovieId();
                Date saleDate = Date.valueOf(LocalDate.now());
                int quantity = item.getQuantity();

                insertStatement.setInt(1, customerId);
                insertStatement.setString(2, movieId);
                insertStatement.setDate(3, saleDate);
                insertStatement.setInt(4, quantity);

                int rowsAffected = insertStatement.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            // Get sale info
                            String saleId = generatedKeys.getString(1);
                            salesIdsArray.add(saleId);
                        }
                    }
                }
            }
        }

        // Clear the cart after purchase is completed
        cart.clear();

        return salesIdsArray;
    }

    private JsonArray createSalesJsonArray(ArrayList<String> salesIds) {
        JsonArray salesJsonArray = new JsonArray();

        for (String saleId : salesIds) {
            salesJsonArray.add(saleId);
        }

        return salesJsonArray;
    }
}
