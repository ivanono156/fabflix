/*
 * This User class only has the id field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final int id;

    public User(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
