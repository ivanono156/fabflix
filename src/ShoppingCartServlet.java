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
import java.util.Optional;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    public static final String shoppingCartAttributeName = "cart_items";

    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    private static final String movieDataQuery = "select title, price from movies where id = ?";

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    // Handles GET requests to display session information
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        request.getServletContext().log("getting " + cart.size() + " items");

        try {
            JsonObject responseJsonObject = getResponseJsonObject(cart);
            out.write(responseJsonObject.toString());

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            JsonObject errorJsonObject = new JsonObject();
            errorJsonObject.addProperty("error", e.getMessage());
            out.write(errorJsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    @Override
    // Handles POST requests to add items to the cart + returns session information
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        String movieId = request.getParameter("id");
        int movieQuantity = Integer.parseInt(request.getParameter("quantity"));

        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cart == null) {
            cart = new ArrayList<>();
            editCart(movieId, movieQuantity, cart);
            session.setAttribute(shoppingCartAttributeName, cart);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cart) {
                editCart(movieId, movieQuantity, cart);
            }
        }

        try {
            JsonObject responseJsonObject = getResponseJsonObject(cart);
            out.write(responseJsonObject.toString());
        } catch (SQLException e) {
            JsonObject errorJsonObject = new JsonObject();
            errorJsonObject.addProperty("error", e.getMessage());
            out.write(errorJsonObject.toString());

            request.getServletContext().log("Error:", e);
        } finally {
            out.close();
        }
    }

    private JsonObject getResponseJsonObject (ArrayList<CartItem> cart) throws SQLException {
        JsonObject responseJsonObject = new JsonObject();

        ArrayList<BigDecimal> prices = new ArrayList<>();
        JsonArray cartItemsJsonArray = getCartItemsArray(cart, prices);

        responseJsonObject.add(shoppingCartAttributeName, cartItemsJsonArray);
        responseJsonObject.addProperty("total_cart_price", getTotalPriceOfCart(prices));

        return responseJsonObject;
    }

    private JsonArray getCartItemsArray(ArrayList<CartItem> cart, ArrayList<BigDecimal> prices) throws SQLException {
        JsonArray cartItemsJsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(movieDataQuery)) {

            for (CartItem item : cart) {
                statement.setString(1, item.getMovieId());

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String movieTitle = rs.getString("title");
                        BigDecimal moviePrice = rs.getBigDecimal("price");
                        BigDecimal totalPrice = getTotalPriceOfItem(item.getQuantity(), moviePrice);

                        JsonObject itemJsonObject = new JsonObject();
                        itemJsonObject.addProperty("movie_id", item.getMovieId());
                        itemJsonObject.addProperty("movie_title", movieTitle);
                        itemJsonObject.addProperty("movie_quantity", item.getQuantity());
                        itemJsonObject.addProperty("movie_price", moviePrice);
                        itemJsonObject.addProperty("total_price", totalPrice);

                        cartItemsJsonArray.add(itemJsonObject);
                        prices.add(totalPrice);
                    }
                }
            }
        }

        return cartItemsJsonArray;
    }

    private void editCart(String movieId, int quantity, ArrayList<CartItem> cart) throws IllegalArgumentException {
        Optional<CartItem> foundItem = cart.stream()
                .filter(item -> item.getMovieId().equals(movieId))
                .findFirst();

        if (foundItem.isPresent()) {
            // quantity will either be '1' for increment, '-1' for decrement, or '0' for clear/remove/delete
            switch (quantity) {
                case -1:
                    foundItem.get().decrementQuantity();
                    break;
                case 0:
                    foundItem.get().clearQuantity();
                    break;
                case 1:
                    foundItem.get().incrementQuantity();
                    break;
                default:
                    throw new IllegalArgumentException("Shopping Cart Servlet: Invalid quantity");
            }

            if (foundItem.get().getQuantity() <= 0) {
                cart.remove(foundItem.get());
            }

        } else {
            // New item
            CartItem newItem = new CartItem(movieId, quantity);
            cart.add(newItem);
        }
    }

    private BigDecimal getTotalPriceOfItem(int quantity, BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal getTotalPriceOfCart(ArrayList<BigDecimal> prices) {
        return prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
