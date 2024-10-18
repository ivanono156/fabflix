import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private final String shoppingCartAttributeName = "cart_items";

    // Handles GET requests to store session information
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Log to localhost log
        request.getServletContext().log("getting " + cartItems.size() + " items");

        JsonObject responseJsonObject = createResponseJsonObject(cartItems);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    // Handles POST requests to add items to the cart
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String movieId = request.getParameter("id");
        String movieTitle = request.getParameter("title");
//        int movieQuantity = Integer.parseInt(request.getParameter("quantity"));
//        double moviePrice = Double.parseDouble(request.getParameter("price"));
        int movieQuantity = 1;
        Random rand = new Random();
        double min = 15.0;
        double max = 25.0;
        double moviePrice = (double) Math.round((min + (max - min) * rand.nextDouble()) * 100) / 100;
        double totalPrice = (double) Math.round(movieQuantity * moviePrice * 100) / 100;

        CartItem newItem = new CartItem(movieId, movieTitle, movieQuantity, moviePrice, totalPrice);

        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            addItemToCart(newItem, cartItems);
            session.setAttribute(shoppingCartAttributeName, cartItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cartItems) {
                addItemToCart(newItem, cartItems);
            }
        }

        JsonObject responseJsonObject = createResponseJsonObject(cartItems);

        response.getWriter().write(responseJsonObject.toString());
    }

    // Create a response json object and add the cart items to the json object
    private JsonObject createResponseJsonObject(ArrayList<CartItem> cartItems) {
        JsonObject responseJsonObject = new JsonObject();
        JsonArray cartItemsJsonArray = new JsonArray();

        for (CartItem item : cartItems) {
            JsonObject itemJsonObject = new JsonObject();
            itemJsonObject.addProperty("movie_id", item.movieId);
            itemJsonObject.addProperty("movie_title", item.movieTitle);
            itemJsonObject.addProperty("movie_quantity", item.quantity);
            itemJsonObject.addProperty("movie_price", item.price);
            itemJsonObject.addProperty("total_price", item.totalPrice);

            cartItemsJsonArray.add(itemJsonObject);
        }

        responseJsonObject.add(shoppingCartAttributeName, cartItemsJsonArray);
        return responseJsonObject;
    }

    private void addItemToCart(CartItem cartItem, ArrayList<CartItem> cartItems) {
        Optional<CartItem> foundItem = cartItems.stream()
                .filter(item -> item.movieId.equals(cartItem.movieId))
                .findFirst();
        if (foundItem.isPresent()) {
            foundItem.get().quantity += cartItem.quantity;
        } else {
            cartItems.add(cartItem);
        }
    }

    static class CartItem {
        private final String movieId;
        private final String movieTitle;
        private int quantity;
        private final double price;
        private double totalPrice;

        public CartItem(String movieId, String movieTitle, int quantity, double price, double totalPrice) {
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.quantity = quantity;
            this.price = price;
            this.totalPrice = totalPrice;
        }
    }
}
