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

    // Handles GET requests to display session information
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Log to localhost log
        request.getServletContext().log("getting " + cart.size() + " items");

        JsonObject responseJsonObject = createResponseJsonObject(cart);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    // Handles POST requests to add items to the cart + returns session information
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

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
        double totalPrice = getTotalPriceOfItem(movieQuantity, moviePrice);

        CartItem newItem = new CartItem(movieId, movieTitle, movieQuantity, moviePrice, totalPrice);

        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute(shoppingCartAttributeName);
        if (cart == null) {
            cart = new ArrayList<>();
            addItemToCart(newItem, cart);
            session.setAttribute(shoppingCartAttributeName, cart);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cart) {
                addItemToCart(newItem, cart);
            }
        }

        JsonObject responseJsonObject = createResponseJsonObject(cart);

        response.getWriter().write(responseJsonObject.toString());
    }

    // Create a response json object and add the cart items to the json object
    private JsonObject createResponseJsonObject(ArrayList<CartItem> cart) {
        JsonObject responseJsonObject = new JsonObject();
        JsonArray cartItemsJsonArray = new JsonArray();

        for (CartItem item : cart) {
            JsonObject itemJsonObject = new JsonObject();
            itemJsonObject.addProperty("movie_id", item.movieId);
            itemJsonObject.addProperty("movie_title", item.movieTitle);
            itemJsonObject.addProperty("movie_quantity", item.quantity);
            itemJsonObject.addProperty("movie_price", item.price);
            itemJsonObject.addProperty("total_price", item.totalPrice);

            cartItemsJsonArray.add(itemJsonObject);
        }

        responseJsonObject.add(shoppingCartAttributeName, cartItemsJsonArray);
        responseJsonObject.addProperty("total_cart_price", getTotalPriceOfCart(cart));

        return responseJsonObject;
    }

    private void addItemToCart(CartItem cartItem, ArrayList<CartItem> cart) {
        Optional<CartItem> foundItem = cart.stream()
                .filter(item -> item.movieId.equals(cartItem.movieId))
                .findFirst();
        if (foundItem.isPresent()) {
            foundItem.get().quantity += cartItem.quantity;
            foundItem.get().totalPrice = getTotalPriceOfItem(foundItem.get().quantity, foundItem.get().price);
        } else {
            cart.add(cartItem);
        }
    }

    private double getTotalPriceOfItem(int quantity, double price) {
        return (double) Math.round(quantity * price * 100) / 100;
    }

    private double getTotalPriceOfCart(ArrayList<CartItem> cart) {
        double totalCartPrice = cart.stream()
                .mapToDouble(item -> item.totalPrice)
                .sum();
        return (double) Math.round(totalCartPrice * 100) / 100;
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
