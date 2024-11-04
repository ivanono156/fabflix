import java.util.HashMap;
import java.util.Iterator;

public class StarsInMoviesSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "casts124.xml";

    private static final String STAR_IN_MOVIE_TAG = "m";
    private static final String MOVIE_ID_TAG = "f";
    private static final String STAR_ID_TAG = "a";

    private StarInMovie tempStarInMovie;

    @Override
    public String getItemType() {
        return "Star In Movie";
    }

    @Override
    protected String getXmlFileName() {
        return XML_FILE_NAME;
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
                validateData(tempStarInMovie);
                break;
            case MOVIE_ID_TAG:
                tempStarInMovie.setMovieId(tempValue);
                break;
            case STAR_ID_TAG:
                tempStarInMovie.setStarId(tempValue);
                break;
        }
    }

    @Override
    protected String getCauseOfInvalidData(DataBaseItem data) {
        StarInMovie starInMovie = (StarInMovie) data;
        if (isDuplicateData(starInMovie)) {
            return "Duplicate star in movie relation";
        } else if (starInMovie.getMovieId() == null || starInMovie.getMovieId().isEmpty()) {
            return "Missing movie id";
        } else if (starInMovie.getStarId() == null || starInMovie.getStarId().isEmpty()) {
            return "Missing star id";
        }
        return "Unknown error while parsing data";
    }
    
    public void setStarInMovieRelations(HashMap<String, DataBaseItem> movies, HashMap<String, DataBaseItem> stars) {
        Iterator<DataBaseItem> starInMoviesIterator = validData.values().iterator();
        while (starInMoviesIterator.hasNext()) {
            StarInMovie starInMovie = (StarInMovie) starInMoviesIterator.next();
            String parsedMovieId = starInMovie.getMovieId();
            String parsedStarId = starInMovie.getStarId();

            if (movies.containsKey(parsedMovieId) && stars.containsKey(parsedStarId)) {
                Star associatedStar = (Star) stars.get(parsedStarId);
                Movie associatedMovie = (Movie) movies.get(parsedMovieId);
                starInMovie.setStar(associatedStar);
                starInMovie.setMovie(associatedMovie);
            } else {
                invalidData.add("Missing star/movie for star in movie relation - " + starInMovie);
                starInMoviesIterator.remove();
            }
        }
    }

    public static void main(String[] args) {
        StarsInMoviesSAXParser parser = new StarsInMoviesSAXParser();
        parser.setDebugMode(DebugMode.OFF);
        parser.run();

        // write to file cuz I can't see everything
        parser.writeToFile();
    }
}
