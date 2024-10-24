public class CartItem {
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

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        this.quantity--;
    }

    public void clearQuantity() {
        this.quantity = 0;
    }
}
