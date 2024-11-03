import java.sql.*;
import java.util.HashMap;
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

    private static final String SELECT_MOVIE_ID_QUERY =
            "select id from movies where title = ? and year = ? and director = ?";
    private static final String SELECT_STAR_ID_QUERY =
            "select id from stars where name = ? and birthYear = ?";

    private static final String SELECT_MAX_MOVIE_ID_QUERY = "select max(id) from movies";
    private static final String SELECT_MAX_STARS_ID_QUERY = "select max(id) from stars";

    private static final int BATCH_AMOUNT = 100;

    private final MovieSAXParser movieSAXParser;
    private final StarSAXParser starSAXParser;
    private final StarsInMoviesSAXParser starsInMoviesSAXParser;

    public XMLParser() {
        movieSAXParser = new MovieSAXParser();
        starSAXParser = new StarSAXParser();
        starsInMoviesSAXParser = new StarsInMoviesSAXParser();
    }

    public void setSAXParserDebugModes(FabflixSAXParser.DebugMode debugMode) {
        movieSAXParser.setDebugMode(debugMode);
        starSAXParser.setDebugMode(debugMode);
        starsInMoviesSAXParser.setDebugMode(debugMode);
    }

    public void run() {
        setSAXParserDebugModes(FabflixSAXParser.DebugMode.OFF);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.out.println("Could not connect to JDBC driver: " + e.getMessage());
        }

        movieSAXParser.run();
        starSAXParser.run();
        starsInMoviesSAXParser.run();
        starsInMoviesSAXParser.setStarInMovieRelations(movieSAXParser.getValidData(), starSAXParser.getValidData());
        starsInMoviesSAXParser.writeToFile("sim_sax_output.txt");

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD)) {
            connection.setAutoCommit(false);

//            handleMovieRecords(connection);
//            handleStarRecords(connection);
//            handleStarInMovieRecords(connection);

//            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }

    private void setRecordId(Connection connection, HashSet<DataBaseItem> dataBaseItems, String selectQuery, String maxId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            String idPrefix = getIdPrefix(maxId);
            int idSuffix = getIdSuffix(maxId);

            for (DataBaseItem dataBaseItem : dataBaseItems) {
                setIdPreparedStatementValues(dataBaseItem, preparedStatement);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String id = resultSet.getString("id");
                        dataBaseItem.setId(id);
                        continue;
                    }
                }

                // FIXME: If record already exists in database, then don't insert it.
                String id = idPrefix + ++idSuffix;
                dataBaseItem.setId(id);
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while setting record ids: " + e.getMessage());
        }
    }

    private String getMaxId(Connection connection, String selectQuery) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("max(id)");
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while getting max ids: " + e.getMessage());
        }
        return "";
    }


    private String getIdPrefix(String id) {
        return id.replaceAll("\\d+", "");
    }

    private int getIdSuffix(String id) {
        return Integer.parseInt(id.replaceAll("\\D+", ""));
    }

    private void handleMovieRecords(Connection connection) {
        HashMap<String, DataBaseItem> validMovies = movieSAXParser.getValidData();
        String maxMovieId = getMaxId(connection, SELECT_MAX_MOVIE_ID_QUERY);
//        setRecordId(connection, new HashSet<>(validMovies.values()), SELECT_MOVIE_ID_QUERY, maxMovieId);

//        int insertedMovies = insertRecordsIntoDataBase(connection, validMovies, INSERT_MOVIE_QUERY);
//        System.out.println(insertedMovies + " movies inserted.");

        HashSet<String> movieGenres = movieSAXParser.getGenres();
//        int insertedGenres = insertGenresIntoDataBase(connection, movieGenres);
//        System.out.println(insertedGenres + " genres inserted.");
    }

    private void handleStarRecords(Connection connection) {
        HashMap<String, DataBaseItem> validStars = starSAXParser.getValidData();
        String maxStarId = getMaxId(connection, SELECT_MAX_STARS_ID_QUERY);
//        setRecordId(connection, new HashSet<>(validStars.values()), SELECT_STAR_ID_QUERY, maxStarId);

//        int insertedStars = insertRecordsIntoDataBase(connection, validStars, INSERT_STAR_QUERY);
//        System.out.println(insertedStars + " stars inserted.");
    }

    private void handleStarInMovieRecords(Connection connection) {
        HashMap<String, DataBaseItem> validStarsInMovies = starsInMoviesSAXParser.getValidData();
//        int insertedStarsInMovies = insertRecordsIntoDataBase(connection, validStarsInMovies, INSERT_STAR_IN_MOVIE_QUERY);
//        System.out.println(insertedStarsInMovies + " stars in movies inserted.");
    }

    private int insertRecordsIntoDataBase(Connection connection, HashSet<DataBaseItem> items, String insertQuery) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
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
            return insertedItems;
        } catch (Exception e) {
            System.out.println("Could not add record to database: " + e.getMessage());
        }
        return 0;
    }

    private int insertGenresIntoDataBase(Connection connection, HashSet<String> genres) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GENRE_QUERY)) {
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

    private void setIdPreparedStatementValues(DataBaseItem data, PreparedStatement preparedStatement) throws Exception {
        if (data instanceof Movie) {
            setMovieIdValues((Movie) data, preparedStatement);
        } else if (data instanceof Star) {
            setStarIdValues((Star) data, preparedStatement);
        } else if (data instanceof StarInMovie) {
//            setStarInMovieValues((StarInMovie) data, preparedStatement);
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
            preparedStatement.setNull(3, Types.INTEGER);
        } else {
            preparedStatement.setInt(3, star.getBirthYear());
        }
    }

    private void setStarInMovieValues(StarInMovie starInMovie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, starInMovie.getStarId());
        preparedStatement.setString(2, starInMovie.getMovieId());
    }

    private void setGenresInMovieValues(Movie movie, PreparedStatement preparedStatement) throws SQLException {

    }

    private void setMovieIdValues(Movie movie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, movie.getTitle());
        preparedStatement.setInt(2, movie.getYear());
        preparedStatement.setString(3, movie.getDirector());
    }

    private void setStarIdValues(Star star, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, star.getName());
        if (star.getBirthYear() == null) {
            preparedStatement.setNull(2, Types.INTEGER);
        } else {
            preparedStatement.setInt(2, star.getBirthYear());
        }
    }

    public static void main(String[] args) {
        XMLParser xmlParser = new XMLParser();
        xmlParser.run();
    }
}
