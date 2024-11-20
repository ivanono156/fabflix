import java.io.PrintWriter;
import java.util.*;

public class StarsInMoviesSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "casts124.xml";

    private static final String STAR_IN_MOVIE_TAG = "m";
    private static final String MOVIE_ID_TAG = "f";
    private static final String STAR_ID_TAG = "a";

    private StarInMovie tempStarInMovie;

    private final StarSAXParser starSAXParser;
    private final MovieSAXParser movieSAXParser;

    public StarsInMoviesSAXParser(StarSAXParser starSAXParser, MovieSAXParser movieSAXParser) {
        super();
        this.starSAXParser = starSAXParser;
        this.movieSAXParser = movieSAXParser;
    }

    @Override
    public String getItemType() {
        return "Star In Movie";
    }

    @Override
    protected String getXmlFileName() {
        return XML_FILE_NAME;
    }

    @Override
    protected void writeToFile(PrintWriter printWriter) {
        printWriter.println("Movies with stars: ");
        for (DataBaseItem data : movieSAXParser.validData.values()) {
            printWriter.println("\t" + data.toString());
        }
    }

    @Override
    protected void addNewData(String qName) {
        if (qName.equalsIgnoreCase(STAR_IN_MOVIE_TAG)) {
            tempStarInMovie = new StarInMovie();
        }
    }

    @Override
    protected void parseItem(String qName) {
        switch (qName.toLowerCase()) {
            case STAR_IN_MOVIE_TAG:
                addStarToMovie(tempStarInMovie);
                break;
            case MOVIE_ID_TAG:
                tempStarInMovie.setMovieId(tempValue);
                break;
            case STAR_ID_TAG:
                tempStarInMovie.setStarId(tempValue);
                break;
        }
    }

    private void addStarToMovie(StarInMovie starInMovie) {
        String movieId = starInMovie.getMovieId();
        String starId = starInMovie.getStarId();

        if (movieSAXParser.validData.containsKey(movieId) && starSAXParser.validData.containsKey(starId)) {
            Movie movie = (Movie) movieSAXParser.validData.get(movieId);
            Star star = (Star) starSAXParser.validData.get(starId);
            movie.addMovieStar(star);
        } else {
            if (!movieSAXParser.validData.containsKey(movieId)) {
                addInvalidData(Error.MOVIE_NOT_FOUND.toString(), tempStarInMovie);
            } else {
                // add the star, since they can have null birth years
                Star newStar = new Star();
                newStar.setId(starId);
                newStar.setName(starId);
                starSAXParser.validData.put(starId, newStar);
                Movie movie = (Movie) movieSAXParser.validData.get(movieId);
                movie.addMovieStar(newStar);
            }
        }
    }

    public void removeMoviesWithoutStars() {
        Iterator<DataBaseItem> iterator = movieSAXParser.validData.values().iterator();
        while (iterator.hasNext()) {
            Movie movie = (Movie) iterator.next();
            if (movie.getMovieStars().isEmpty()) {
                movieSAXParser.addInvalidData(Error.MOVIE_WITHOUT_STAR.toString(), movie);
                iterator.remove();
            }
        }
    }

    @Override
    protected String getCauseOfInvalidData(DataBaseItem data) {
        StarInMovie starInMovie = (StarInMovie) data;
        if (isDuplicateData(starInMovie)) {
            return Error.DUPLICATE.getDescription();
        } else if (starInMovie.getMovieId() == null || starInMovie.getMovieId().isEmpty()) {
            return Error.MOVIE_NOT_FOUND.getDescription();
        } else if (starInMovie.getStarId() == null || starInMovie.getStarId().isEmpty()) {
            return Error.STAR_NOT_FOUND.getDescription();
        }
        return "Unknown error while parsing data";
    }

    public static void main(String[] args) {
        StarSAXParser starSAXParser = new StarSAXParser();
        MovieSAXParser movieSAXParser = new MovieSAXParser();
        starSAXParser.setDebugMode(DebugMode.OFF);
        movieSAXParser.setDebugMode(DebugMode.OFF);
        starSAXParser.run();
        movieSAXParser.run();
        StarsInMoviesSAXParser parser = new StarsInMoviesSAXParser(starSAXParser, movieSAXParser);
        parser.setDebugMode(DebugMode.OFF);
        parser.run();

        // write to file cuz I can't see everything
        parser.writeToFile();
    }
}
