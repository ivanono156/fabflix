import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class StarsInMoviesSAXParser extends FabflixSAXParser {
    private static final String XML_FILE_NAME = "casts124.xml";

    private static final String STAR_IN_MOVIE_TAG = "m";
    private static final String MOVIE_ID_TAG = "f";
    private static final String STAR_ID_TAG = "a";

    private StarInMovie tempStarInMovie;

    public void writeToFile(String file) {
        HashSet<DataBaseItem> validData = getValidData();
        ArrayList<DataBaseItem> invalidData = getInvalidData();
        ArrayList<String> brokenAttributes = getBrokenAttributes();

        try (FileWriter fileWriter = new FileWriter(file);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            printWriter.println("Number of valid items found: " + validData.size());
            for (DataBaseItem data : validData) {
                printWriter.println("\t" + data.toString());
            }

            printWriter.println("Number of broken attributes found: " + brokenAttributes.size());
            for (String attr : brokenAttributes) {
                printWriter.println("\t" + attr);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
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

    public static void main(String[] args) {
        StarsInMoviesSAXParser parser = new StarsInMoviesSAXParser();
        parser.setDebugMode(DebugMode.OFF);
        parser.run();

        // write to file cuz I can't see everything
        String outputFile = "sim_sax_output.txt";
        parser.writeToFile(outputFile);
    }
}
