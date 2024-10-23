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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Optional;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    public static final String shoppingCartAttributeName = "cart_items";

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Handles GET requests to display session information
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        request.getServletContext().log("getting " + cart.size() + " items");

        JsonObject responseJsonObject = getResponseJsonObject(cart);

        response.getWriter().write(responseJsonObject.toString());
    }

    // Handles POST requests to add items to the cart + returns session information
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

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

        JsonObject responseJsonObject = getResponseJsonObject(cart);

        response.getWriter().write(responseJsonObject.toString());
    }

    private JsonObject getResponseJsonObject (ArrayList<CartItem> cart) {
        JsonObject responseJsonObject = new JsonObject();
        ArrayList<BigDecimal> prices = new ArrayList<>();
        JsonArray cartItemsJsonArray = getCartItemsArray(cart, prices);

        responseJsonObject.add(shoppingCartAttributeName, cartItemsJsonArray);
        responseJsonObject.addProperty("total_cart_price", getTotalPriceOfCart(prices));

        return responseJsonObject;
    }

    private JsonArray getCartItemsArray(ArrayList<CartItem> cart, ArrayList<BigDecimal> prices) {
        JsonArray cartItemsJsonArray = new JsonArray();
        String query  = "select title, price from movies where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            for (CartItem item : cart) {
                statement.setString(1, item.movieId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String movieTitle = rs.getString("title");
                        BigDecimal moviePrice = rs.getBigDecimal("price");
                        BigDecimal totalPrice = getTotalPriceOfItem(item.quantity, moviePrice);

                        JsonObject itemJsonObject = new JsonObject();
                        itemJsonObject.addProperty("movie_id", item.movieId);
                        itemJsonObject.addProperty("movie_title", movieTitle);
                        itemJsonObject.addProperty("movie_quantity", item.quantity);
                        itemJsonObject.addProperty("movie_price", moviePrice);
                        itemJsonObject.addProperty("total_price", totalPrice);

                        cartItemsJsonArray.add(itemJsonObject);
                        prices.add(totalPrice);
                    }
                }
            }
        } catch (Exception e) {

        }
        return cartItemsJsonArray;
    }

    private void editCart(String movieId, int quantity, ArrayList<CartItem> cart) throws IllegalArgumentException {
        Optional<CartItem> foundItem = cart.stream()
                .filter(item -> item.movieId.equals(movieId))
                .findFirst();

        if (foundItem.isPresent()) {
            // quantity will either be '1' for increase, '-1' for decrease, or '0' for remove/delete
            switch (quantity) {
                case -1:
                    foundItem.get().quantity -= 1;
                    break;
                case 0:
                    foundItem.get().quantity = 0;
                    break;
                case 1:
                    foundItem.get().quantity += 1;
                    break;
                default:
                    throw new IllegalArgumentException("Shopping Cart Servlet: Invalid quantity");
            }

            if (foundItem.get().quantity <= 0) {
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

    static class CartItem {
        private final String movieId;
        private int quantity;

        public CartItem(String movieId, int quantity) {
            this.movieId = movieId;
            this.quantity = quantity;
        }

        public String getMovieId() {
            return this.movieId;
        }

        public int getQuantity() {
            return this.quantity;
        }
    }
}
