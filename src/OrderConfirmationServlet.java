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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name="OrderConfirmationServlet", urlPatterns = "/api/order-confirmation")
public class OrderConfirmationServlet extends HttpServlet {

    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    private final static String salesQuery = "select s.id, m.title, m.price, s.quantity " +
            "from sales as s inner join movies as m on s.movieId = m.id " +
            "where s.id = ?";

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        try (Connection conn = dataSource.getConnection()){

            ArrayList<String> saleIds = (ArrayList<String>) session.getAttribute(PaymentServlet.salesIdsAttributeName);

            if (saleIds == null) {
                return;
            }

            ArrayList<BigDecimal> prices = new ArrayList<>();

            JsonArray salesJsonArray = getSaleInfo(conn, saleIds, prices);
            BigDecimal totalCartPrice = getTotalPriceOfCart(prices);

            session.removeAttribute(PaymentServlet.salesIdsAttributeName);

            JsonObject jsonObject = new JsonObject();

            jsonObject.add("sales", salesJsonArray);
            jsonObject.addProperty("total_cart_price", totalCartPrice);

            // Write JSON string to output
            out.write(jsonObject.toString());
            // Set response status to 200 (OK)
            response.setStatus(HttpServletResponse.SC_OK);
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

    private JsonArray getSaleInfo(Connection conn, ArrayList<String> saleIds, ArrayList<BigDecimal> prices) throws SQLException {
        JsonArray jsonArray = new JsonArray();

        try (PreparedStatement statement = conn.prepareStatement(salesQuery)) {
            for (String saleId : saleIds) {
                // Retrieve sales info from database
                statement.setString(1, saleId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        JsonObject sale = new JsonObject();

                        String movieTitle = rs.getString("title");
                        int quantity = rs.getInt("quantity");
                        BigDecimal price = rs.getBigDecimal("price");
                        BigDecimal totalPrice = getTotalPriceOfItem(quantity, price);

                        sale.addProperty("sale_id", saleId);
                        sale.addProperty("movie_title", movieTitle);
                        sale.addProperty("movie_quantity", quantity);
                        sale.addProperty("movie_price", price);
                        sale.addProperty("total_movie_price", totalPrice);

                        jsonArray.add(sale);

                        prices.add(totalPrice);
                    }
                }
            }
        }

        return jsonArray;
    }

    private BigDecimal getTotalPriceOfItem(int quantity, BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal getTotalPriceOfCart(ArrayList<BigDecimal> prices) {
        return prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
