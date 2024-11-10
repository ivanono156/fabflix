import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class XMLParser {
    String MYSQL_USER = "mytestuser";
    String MYSQL_PASSWORD = "My6$Password";
    String MYSQL_URL = "jdbc:mysql://localhost:3306/moviedb";

    private static final String INSERT_MOVIE_QUERY = "insert into movies (id, title, year, director, price) " +
            "values (?, ?, ?, ?, floor(10 + rand() * 100 - 10))";
    private static final String INSERT_STAR_QUERY = "insert into stars (id, name, birthYear) values (?, ?, ?)";
    private static final String INSERT_STAR_IN_MOVIE_QUERY = "insert into stars_in_movies (starId, movieId) " +
            "values (?, ?)";
    private static final String INSERT_GENRE_QUERY = "insert into genres (name) values (?)";
    private static final String INSERT_GENRE_IN_MOVIE_QUERY = "insert into genres_in_movies (genreId, movieId) " +
            "values (?, ?)";

    private static final String SELECT_MOVIE_ID_QUERY = "select id from movies " +
            "where title = ? and year = ? and director = ?";
    private static final String SELECT_STAR_ID_QUERY = "select id from stars where name = ? and birthYear = ?";
    private static final String SELECT_GENRES_QUERY = "select * from genres";

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

        try (Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD)) {
            connection.setAutoCommit(false);

            handleRecords(connection);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }

//        writeParsersOutputToFile();
        printErrors();
    }

    public void writeParsersOutputToFile() {
        try (FileWriter fileWriter = new FileWriter(FabflixSAXParser.OUTPUT_FILE);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            movieSAXParser.writeToFile(printWriter);
            starSAXParser.writeToFile(printWriter);
            starsInMoviesSAXParser.writeToFile(printWriter);

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    private void handleRecords(Connection connection) {
        HashMap<String, DataBaseItem> validStars = prepareStarRecords(connection);
        HashMap<String, DataBaseItem> validMovies = prepareMovieRecords(connection);
        prepareStarInMovieRecords(validMovies, validStars);
        HashSet<String> newGenres = prepareGenreRecords(connection);

        int insertedStars = insertRecordsIntoDataBase(connection, validStars.values(), INSERT_STAR_QUERY);
        System.out.println(insertedStars + " stars inserted.");

        int insertedMovies = insertRecordsIntoDataBase(connection, validMovies.values(), INSERT_MOVIE_QUERY);
        System.out.println(insertedMovies + " movies inserted.");

        int insertedGenres = insertGenresIntoDataBase(connection, newGenres);
        System.out.println(insertedGenres + " genres inserted.");

        int insertedGenresInMovies = insertGenresInMoviesIntoDataBase(connection, validMovies, getDataBaseGenres(connection));
        System.out.println(insertedGenresInMovies + " genres in movies inserted.");

        HashMap<String, DataBaseItem> validStarsInMovies = starsInMoviesSAXParser.getValidData();
        int insertedStarsInMovies = insertRecordsIntoDataBase(connection, validStarsInMovies.values(), INSERT_STAR_IN_MOVIE_QUERY);
        System.out.println(insertedStarsInMovies + " stars in movies inserted.");
    }

    public void printErrors() {
        HashMap<String, ArrayList<DataBaseItem>> invalidMovies = movieSAXParser.getInvalidData();
        ArrayList<DataBaseItem> inconsistentMovies = invalidMovies.get(FabflixSAXParser.Error.INCONSISTENT.getDescription());
        System.out.println(inconsistentMovies.size() + " movies inconsistent");

        ArrayList<DataBaseItem> duplicateMovies = invalidMovies.get(FabflixSAXParser.Error.DUPLICATE.getDescription());
        System.out.println(duplicateMovies.size() + " movies duplicate");

        HashMap<String, ArrayList<DataBaseItem>> invalidStars = starSAXParser.getInvalidData();
        ArrayList<DataBaseItem> duplicateStars = invalidStars.get(FabflixSAXParser.Error.DUPLICATE.getDescription());
        System.out.println(duplicateStars.size() + " stars duplicate");

        HashMap<String, ArrayList<DataBaseItem>> invalidStarsInMovies = starsInMoviesSAXParser.getInvalidData();
        ArrayList<DataBaseItem> notFoundMovies = invalidStarsInMovies.get(FabflixSAXParser.Error.MOVIE_NOT_FOUND.getDescription());
        System.out.println(notFoundMovies.size() + " movies not found");

        ArrayList<DataBaseItem> notFoundStars = invalidStarsInMovies.get(FabflixSAXParser.Error.STAR_NOT_FOUND.getDescription());
        System.out.println(notFoundStars.size() + " stars not found");
    }

    private void prepareStarInMovieRecords(HashMap<String, DataBaseItem> movies, HashMap<String, DataBaseItem> stars) {
        starsInMoviesSAXParser.setStarInMovieRelations(movies, stars);
        removeMoviesWithoutStars(movies.values());
        removeStarsWithoutMovies(stars.values());
    }

    private HashMap<String, DataBaseItem> prepareStarRecords(Connection connection) {
        HashMap<String, DataBaseItem> validStars = starSAXParser.getValidData();
        String maxStarId = getMaxId(connection, SELECT_MAX_STARS_ID_QUERY);
        setRecordId(connection, starSAXParser, validStars, SELECT_STAR_ID_QUERY, maxStarId);
        return validStars;
    }

    private HashMap<String, DataBaseItem> prepareMovieRecords(Connection connection) {
        HashMap<String, DataBaseItem> validMovies = movieSAXParser.getValidData();
        String maxMovieId = getMaxId(connection, SELECT_MAX_MOVIE_ID_QUERY);
        setRecordId(connection, movieSAXParser, validMovies, SELECT_MOVIE_ID_QUERY, maxMovieId);
        movieSAXParser.setMoviesWithoutAGenreToUncategorizedGenre();
        return validMovies;
    }

    private HashSet<String> prepareGenreRecords(Connection connection) {
        HashSet<String> newGenres = movieSAXParser.getGenres();
        HashMap<String, Integer> dataBaseGenres = getDataBaseGenres(connection);
        newGenres.removeIf(dataBaseGenres::containsKey);
        return newGenres;
    }

    private void setRecordId(Connection connection, FabflixSAXParser parser, HashMap<String, DataBaseItem> dataBaseItems,
                             String selectQuery, String maxId) {

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            String idPrefix = getIdPrefix(maxId);
            int idSuffix = getIdSuffix(maxId);

            Iterator<DataBaseItem> iterator = dataBaseItems.values().iterator();
            while (iterator.hasNext()) {
                DataBaseItem dataBaseItem = iterator.next();
                setPreparedStatementValuesForRetrievingIds(dataBaseItem, preparedStatement);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String id = resultSet.getString("id");
                        dataBaseItem.setId(id);
                        // If record already exists in database, then don't insert it.
                        iterator.remove();
                        parser.addInvalidData(FabflixSAXParser.Error.DUPLICATE.getDescription(), dataBaseItem);
                        continue;
                    }
                }

                String id = idPrefix + ++idSuffix;
                dataBaseItem.setId(id);
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while setting record ids: " + e.getMessage());
        }
    }

    private int insertGenresInMoviesIntoDataBase(Connection connection, HashMap<String,DataBaseItem> validMovies, HashMap<String, Integer> dataBaseGenres) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GENRE_IN_MOVIE_QUERY)) {
            int insertedItems = 0;
            int i = 0;
            int totalGenres = 0;
            for (DataBaseItem dataBaseItem : validMovies.values()) {
                Movie movie = (Movie) dataBaseItem;
                totalGenres += movie.getGenres().size();
            }

            for (DataBaseItem dataBaseItem : validMovies.values()) {
                Movie movie = (Movie) dataBaseItem;
                for (String genre : movie.getGenres()) {
                    int genreId = dataBaseGenres.get(genre);
                    preparedStatement.setInt(1, genreId);
                    preparedStatement.setString(2, movie.getId());
                    preparedStatement.addBatch();

                    if (i % BATCH_AMOUNT == 0 || i == totalGenres - 1) {
                        int[] updateCounts = preparedStatement.executeBatch();
                        insertedItems += updateCounts.length;
                    }
                    i++;
                }
            }
            return insertedItems;
        } catch (SQLException se) {
            System.out.println("SQLException: while inserting genres in movies" + se.getMessage());
        }
        return 0;
    }

    private int insertRecordsIntoDataBase(Connection connection, Collection<DataBaseItem> items, String insertQuery) {
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
            setValues((Movie) data, preparedStatement);
        } else if (data instanceof Star) {
            setValues((Star) data, preparedStatement);
        } else if (data instanceof StarInMovie) {
            setStarInMovieValues((StarInMovie) data, preparedStatement);
        } else {
            throw new Exception("Unknown data type: " + data.getClass());
        }
    }

    private void setPreparedStatementValuesForRetrievingIds(DataBaseItem data, PreparedStatement preparedStatement) throws Exception {
        if (data instanceof Movie) {
            setMovieValues(1, (Movie) data, preparedStatement);
        } else if (data instanceof Star) {
            setStarValues(1, (Star) data, preparedStatement);
        } else if (data instanceof StarInMovie) {
            setStarInMovieValues((StarInMovie) data, preparedStatement);
        } else {
            throw new Exception("Unknown data type: " + data.getClass());
        }
    }

    private void setValues(Movie movie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, movie.getId());
        setMovieValues(2, movie, preparedStatement);
    }

    private void setValues(Star star, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, star.getId());
        setStarValues(2, star, preparedStatement);
    }

    private void setStarInMovieValues(StarInMovie starInMovie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, starInMovie.getStarId());
        preparedStatement.setString(2, starInMovie.getMovieId());
    }

    private void setMovieValues(int startIndex, Movie movie, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(startIndex, movie.getTitle());
        preparedStatement.setInt(startIndex + 1, movie.getYear());
        preparedStatement.setString(startIndex + 2, movie.getDirector());
    }

    private void setStarValues(int startIndex, Star star, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(startIndex, star.getName());
        if (star.getBirthYear() == null) {
            preparedStatement.setNull(startIndex + 1, Types.INTEGER);
        } else {
            preparedStatement.setInt(startIndex + 1, star.getBirthYear());
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

    private HashMap<String, Integer> getDataBaseGenres(Connection connection) {
        HashMap<String, Integer> genres = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_GENRES_QUERY);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Integer genreId = resultSet.getInt("id");
                String genreName = resultSet.getString("name");
                genres.put(genreName, genreId);
            }
        } catch (SQLException se) {
            System.out.println("SQL Exception while getting genres: " + se.getMessage());
        }
        return genres;
    }

    private void removeMoviesWithoutStars(Collection<DataBaseItem> validMovies) {
        Iterator<DataBaseItem> iterator = validMovies.iterator();
        while (iterator.hasNext()) {
            Movie movie = (Movie) iterator.next();
            if (movie.getMovieStars().isEmpty()) {
                movieSAXParser.addInvalidData(FabflixSAXParser.Error.STAR_NOT_FOUND.getDescription(), movie);
                iterator.remove();
            }
        }
    }

    private void removeStarsWithoutMovies(Collection<DataBaseItem> validStars) {
        Iterator<DataBaseItem> iterator = validStars.iterator();
        while (iterator.hasNext()) {
            Star star = (Star) iterator.next();
            if (star.getMovies().isEmpty()) {
                starSAXParser.addInvalidData(FabflixSAXParser.Error.MOVIE_NOT_FOUND.getDescription(), star);
                iterator.remove();
            }
        }
    }

    public static void main(String[] args) {
        XMLParser xmlParser = new XMLParser();
        xmlParser.run();
    }
}
