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

    private HashSet<String> genres;

    public MovieSAXParser() {
        super();
        genres = new HashSet<>();
    }

    @Override
    protected String getXmlFileName() {
        return XML_FILE_NAME;
    }

    @Override
    protected void printData() {
        super.printData();

        System.out.println("Number of genres: " + genres.size());
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
        if (qName.equalsIgnoreCase(MOVIE_TAG)) {
            addDataToList(tempMovie);
        } else if (qName.equalsIgnoreCase(ID_TAG)) {
            tempMovie.setId(tempValue);
        } else if (qName.equalsIgnoreCase(TITLE_TAG)) {
            tempMovie.setTitle(tempValue);
        } else if (qName.equalsIgnoreCase(YEAR_TAG)) {
            tempMovie.setYear(parseIntValue(tempValue));
        } else if (qName.equalsIgnoreCase(DIRECTOR_TAG)) {
            tempMovie.setDirector(tempValue);
        } else if (qName.equalsIgnoreCase(GENRE_TAG)) {
            addGenreToMovie(tempValue);
        }
    }

    private void addGenreToMovie(String genre) {
        if (!genre.isEmpty()) {
            tempMovie.addGenre(genre);
            genres.add(genre);
        }
    }

    @Override
    protected boolean isValidItem(DataBaseItem item) {
        Movie movie = (Movie) item;
        return movie.getId() != null && !movie.getId().isEmpty()
                && movie.getTitle() != null && !movie.getTitle().isEmpty()
                && movie.getYear() != -1
                && movie.getDirector() != null && !movie.getDirector().isEmpty();
    }

    public static void main(String[] args) {
        MovieSAXParser parser = new MovieSAXParser();
        parser.run();
    }
}
