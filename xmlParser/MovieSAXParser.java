import java.util.HashSet;

public class MovieSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "mains243.xml";

    private static final String MOVIE_TAG = "film";
    private static final String ID_TAG = "fid";
    private static final String TITLE_TAG = "t";
    private static final String YEAR_TAG = "year";
    private static final String DIRECTOR_TAG = "dirn";
    private static final String GENRE_TAG = "cat";

    private Movie tempMovie;

    @Override
    public String getItemType() {
        return "Movie";
    }

    @Override
    protected String getXmlFileName() {
        return XML_FILE_NAME;
    }

    @Override
    protected void printData() {
        super.printData();

        HashSet<String> genres = getGenres();
        System.out.println("Number of unique genres found: " + genres.size());
        for (String genre : genres) {
            System.out.println("\t" + genre);
        }
    }

    @Override
    protected void addNewData(String qName) {
        if (qName.equals(MOVIE_TAG)) {
            tempMovie = new Movie();
        }
    }

    @Override
    protected void parseItem(String qName) {
        switch (qName.toLowerCase()) {
            case MOVIE_TAG:
                validateData(tempMovie);
                break;
            case ID_TAG:
                tempMovie.setId(tempValue);
                break;
            case TITLE_TAG:
                tempMovie.setTitle(tempValue);
                break;
            case YEAR_TAG:
                tempMovie.setYear(parseIntValue(tempValue));
                break;
            case DIRECTOR_TAG:
                tempMovie.setDirector(tempValue);
                break;
            case GENRE_TAG:
                addGenreToMovie(tempMovie, tempValue);
                break;
        }
    }

    private void addGenreToMovie(Movie movie, String genre) {
        if (!genre.isEmpty()) {
            movie.addGenre(genre);
        }
    }

    @Override
    protected String getCauseOfInvalidData(DataBaseItem data) {
        Movie movie = (Movie) data;
        if (isDuplicateData(movie)) {
            return "Duplicate movie id";
        } else if (movie.getId() == null || movie.getId().isEmpty()) {
            return "Missing movie id";
        } else if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            return "Missing movie title";
        } else if (movie.getYear() == -1) {
            return "Error parsing movie year";
        } else if (movie.getDirector() == null || movie.getDirector().isEmpty()) {
            return "Missing movie director";
        }
        return "Unknown error while parsing data";
    }

    public HashSet<String> getGenres() {
        HashSet<String> genres = new HashSet<>();
        for (DataBaseItem data : validData.values()) {
            Movie movie = (Movie) data;
            genres.addAll(movie.getGenres());
        }
        return genres;
    }

    public static void main(String[] args) {
        MovieSAXParser parser = new MovieSAXParser();
        parser.run();
    }
}
