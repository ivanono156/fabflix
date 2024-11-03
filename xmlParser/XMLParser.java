import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class XMLParser {
    String MYSQL_USER = "mytestuser";
    String MYSQL_PASSWORD = "My6$Password";
    String MYSQL_URL = "jdbc:mysql://localhost:3306/moviedb";

    private static final String INSERT_MOVIE_QUERY =
            "insert into movies (id, title, year, director, price) values (?, ?, ?, ?, floor(10 + rand() * 100 - 10))";
    private static final String INSERT_STAR_QUERY =
            "insert into stars (id, name, birthYear) values (?, ?, ?)";
    private static final String INSERT_STAR_IN_MOVIE_QUERY =
            "insert into stars_in_movies (starId, movieId) values (?, ?)";
    private static final String INSERT_GENRE_QUERY =
            "insert into genres (name) values (?)";
    private static final String INSERT_GENRE_IN_MOVIE_QUERY =
            "insert into genres_in_movies (genreId, movieId) values (?, ?)";

    private static final int BATCH_AMOUNT = 100;

    private MovieSAXParser movieSAXParser;
    private StarSAXParser starSAXParser;
    private StarsInMoviesSAXParser starsInMoviesSAXParser;

    public XMLParser() {
        movieSAXParser = new MovieSAXParser();
        starSAXParser = new StarSAXParser();
        starsInMoviesSAXParser = new StarsInMoviesSAXParser();
    }

    public void run() {
        setSAXParserDebugModes(FabflixSAXParser.DebugMode.OFF);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.out.println("Could not connect to JDBC driver: " + e.getMessage());
        }

        runMovieParser();
//        runStarParser();
//        runStarsInMoviesParser();
    }

    public void setSAXParserDebugModes(FabflixSAXParser.DebugMode debugMode) {
        movieSAXParser.setDebugMode(debugMode);
        starSAXParser.setDebugMode(debugMode);
        starsInMoviesSAXParser.setDebugMode(debugMode);
    }

    private void runMovieParser() {
        movieSAXParser.run();
        HashSet<DataBaseItem> validMovies = movieSAXParser.getValidData();
        int insertedMovies = insertIntoDataBase(validMovies, INSERT_MOVIE_QUERY);
        System.out.println(insertedMovies + " movies inserted.");
        HashSet<String> movieGenres = movieSAXParser.getGenres();
        System.out.println("Genres found: " + movieGenres.size());
    }

    private void runStarParser() {
        starSAXParser.run();
        HashSet<DataBaseItem> validStars = starSAXParser.getValidData();
        int insertedStars = insertIntoDataBase(validStars, INSERT_STAR_QUERY);
        System.out.println(insertedStars + " stars inserted.");
    }

    private void runStarsInMoviesParser() {
        starsInMoviesSAXParser.run();
        HashSet<DataBaseItem> validStarsInMovies = starsInMoviesSAXParser.getValidData();
        int insertedStarsInMovies = insertIntoDataBase(validStarsInMovies, INSERT_STAR_IN_MOVIE_QUERY);
        System.out.println(insertedStarsInMovies + " stars in movies inserted.");
    }

    private int insertIntoDataBase(HashSet<DataBaseItem> items, String insertQuery) {
        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            connection.setAutoCommit(false);

            int insertedItems = 0;
            int i = 0;
            for (DataBaseItem item : items) {
                setPreparedStatementValues(item, preparedStatement);
                preparedStatement.addBatch();

                if (i % BATCH_AMOUNT == 0 || i == items.size() - 1) {
                    int[] updateCounts = preparedStatement.executeBatch();
                    insertedItems += updateCounts.length;
                }
                i++;
            }

            connection.commit();
            connection.setAutoCommit(true);
            return insertedItems;
        } catch (Exception e) {
            System.out.println("Could not add " +  items.getClass().getName() + " to database: " + e.getMessage());
        }

        return 0;
    }

    private int insertGenresIntoDataBase(HashSet<String> genres) {
        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GENRE_QUERY)) {
            connection.setAutoCommit(false);

            int insertedGenres = 0;
            int i = 0;
            for (String genre : genres) {
                preparedStatement.setString(1, genre);
                preparedStatement.addBatch();

                if (i % BATCH_AMOUNT == 0 || i == genres.size() - 1) {
                    int[] updateCounts = preparedStatement.executeBatch();
                    insertedGenres += updateCounts.length;
                }
                i++;
            }

            connection.commit();
            connection.setAutoCommit(true);
            return insertedGenres;
        } catch (SQLException e) {
            System.out.println("Could not add genres to database: " + e.getMessage());
        }
        return 0;
    }

    private void setPreparedStatementValues(DataBaseItem data, PreparedStatement preparedStatement) throws Exception {
        if (data instanceof Movie) {
            setMovieValues((Movie) data, preparedStatement);
        } else if (data instanceof Star) {
            setStarValues((Star) data, preparedStatement);
        } else if (data instanceof StarInMovie) {
            setStarInMovieValues((StarInMovie) data, preparedStatement);
        } else {
            throw new Exception("Unknown data type: " + data.getClass());
        }
    }

    private void setMovieValues(Movie movie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, movie.getId());
        preparedStatement.setString(2, movie.getTitle());
        preparedStatement.setInt(3, movie.getYear());
        preparedStatement.setString(4, movie.getDirector());
    }

    private void setStarValues(Star star, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, star.getId());
        preparedStatement.setString(2, star.getName());
        if (star.getBirthYear() == null) {
            preparedStatement.setInt(3, star.getBirthYear());
        } else {
            preparedStatement.setNull(3, star.getBirthYear());
        }
    }

    private void setStarInMovieValues(StarInMovie starInMovie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, starInMovie.getStarId());
        preparedStatement.setString(2, starInMovie.getMovieId());
    }

    private void setGenresInMovieValues(Movie movie, PreparedStatement preparedStatement) throws SQLException {

    }

    public static void main(String[] args) {
        XMLParser xmlParser = new XMLParser();
        xmlParser.run();
    }
}
