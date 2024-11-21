public final class JdbcConstants {
    public static final String readWriteDataSourceURL = "java:comp/env/jdbc/moviedbReadWrite";
    public static final String readOnlyDataSourceURL = "java:comp/env/jdbc/moviedbReadOnly";

    private JdbcConstants() {
        // Unused
    }
}
