import java.util.HashMap;
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

    public static final HashMap<String, String> categoriesToGenres;
    static {
        categoriesToGenres = new HashMap<>();
        categoriesToGenres.put("ctxx", "Uncategorized");    // Ctxx
        categoriesToGenres.put("actn", "Action");   // Actn: violence
        categoriesToGenres.put("camp", "Camp");
        categoriesToGenres.put("disa", "Disaster");
        categoriesToGenres.put("epic", "Epic");
        categoriesToGenres.put("scfi", "Sci-Fi");   // science fiction
        categoriesToGenres.put("cart", "Animation");
        categoriesToGenres.put("faml", "Family");
        categoriesToGenres.put("surl", "Surreal");
        categoriesToGenres.put("avga", "Avant Garde");  // AvGa
        categoriesToGenres.put("hist", "History");

        categoriesToGenres.put("susp", "Thriller");
        categoriesToGenres.put("cnr", "Crime");  // CnR: Cops and Robbers
        categoriesToGenres.put("cnrb", "Crime");  // CnRb
        categoriesToGenres.put("dram", "Drama");
        categoriesToGenres.put("h", "Drama");
        categoriesToGenres.put("west", "Western");
        categoriesToGenres.put("myst", "Mystery");
        categoriesToGenres.put("sf", "Sci-Fi");   // science fiction
        categoriesToGenres.put("advt", "Adventure");
        categoriesToGenres.put("horr", "Horror");
        categoriesToGenres.put("romt", "Romance");  // Romantic
        categoriesToGenres.put("comd", "Comedy");
        categoriesToGenres.put("musc", "Musical");
        categoriesToGenres.put("docu", "Documentary");
        categoriesToGenres.put("porn", "Adult");    // Pornography
        categoriesToGenres.put("noir", "Noir"); // Black
        categoriesToGenres.put("biop", "Biographical Picture");
        categoriesToGenres.put("tv", "TV Show");
        categoriesToGenres.put("tvs", "TV Series");
        categoriesToGenres.put("tvm", "TV MiniSeries");
    }

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
        String newGenre = genre.replaceAll("[^a-zA-Z]", "").toLowerCase();
        if (!newGenre.isEmpty()) {
            if (categoriesToGenres.containsKey(newGenre)) {
                movie.addGenre(categoriesToGenres.get(newGenre));
            } else if (newGenre.length() > 1) {
                movie.addGenre(newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1).toLowerCase());
            } else {
                movie.addGenre(newGenre.substring(0, 1).toUpperCase());
            }
        }
    }

    @Override
    protected String getCauseOfInvalidData(DataBaseItem data) {
        Movie movie = (Movie) data;
        if (isDuplicateData(movie)) {
            return Error.DUPLICATE.getDescription();
        } else if (movie.getId() == null || movie.getId().isEmpty()) {
            return Error.INCONSISTENT.getDescription();
        } else if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            return Error.INCONSISTENT.getDescription();
        } else if (movie.getYear() == invalidIntValue) {
            return Error.INCONSISTENT.getDescription();
        } else if (movie.getDirector() == null || movie.getDirector().isEmpty()) {
            return Error.INCONSISTENT.getDescription();
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

    public void setMoviesWithoutAGenreToUncategorizedGenre() {
        for (DataBaseItem item : validData.values()) {
            Movie movie = (Movie) item;
            if (movie.getGenres().isEmpty()) {
                movie.addGenre(categoriesToGenres.get("ctxx")); // Uncategorized
            }
        }
    }

    public static void main(String[] args) {
        MovieSAXParser parser = new MovieSAXParser();
        parser.run();
    }
}
