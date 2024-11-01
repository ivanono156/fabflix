public class StarsInMoviesSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "casts124.xml";

    private static final String STAR_IN_MOVIE_TAG = "m";
    private static final String MOVIE_ID_TAG = "f";
    private static final String STAR_ID_TAG = "a";

    private StarInMovie tempStarInMovie;

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
        if (qName.equalsIgnoreCase(STAR_IN_MOVIE_TAG)) {
            validateData(tempStarInMovie);
        } else if (qName.equalsIgnoreCase(MOVIE_ID_TAG)) {
            tempStarInMovie.setMovieId(tempValue);
        } else if (qName.equalsIgnoreCase(STAR_ID_TAG)) {
            tempStarInMovie.setStarId(tempValue);
        }
    }

    @Override
    protected String getCauseOfInvalidData(DataBaseItem data) {
        StarInMovie starInMovie = (StarInMovie) data;
        if (starInMovie.getMovieId() == null || starInMovie.getMovieId().isEmpty()) {
            return "Missing movie id";
        } else if (starInMovie.getStarId() == null || starInMovie.getStarId().isEmpty()) {
            return "Missing star id";
        }
        return "Unknown error while parsing data";
    }

    @Override
    protected boolean isValidData(DataBaseItem data) {
        StarInMovie starInMovie = (StarInMovie) data;
        return starInMovie.getStarId() != null && !starInMovie.getStarId().isEmpty()
                && starInMovie.getMovieId() != null && !starInMovie.getMovieId().isEmpty();
    }

    public static void main(String[] args) {
        StarsInMoviesSAXParser parser = new StarsInMoviesSAXParser();
        parser.run();
    }
}
