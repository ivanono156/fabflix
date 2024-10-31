import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class MovieSAXParser extends DefaultHandler {
    private static final String XML_FILE_PATH =
            "C:\\Users\\Ivan Onofre\\University\\CS 122B\\stanford-movies\\mains243.xml";
    private static final String ENCODING = "ISO-8859-1";

    private static final String MOVIE_TAG = "film";
    private static final String ID_TAG = "fid";
    private static final String TITLE_TAG = "t";
    private static final String YEAR_TAG = "year";
    private static final String DIRECTOR_TAG = "dirn";
    private static final String GENRE_TAG = "cat";

    ArrayList<Movie> validMovies;
    ArrayList<Movie> brokenMovies;
    ArrayList<String> brokenAttributes;
    HashSet<String> genres;

    private String tempValue;
    private Movie tempMovie;

    public MovieSAXParser() {
        validMovies = new ArrayList<>();
        brokenMovies = new ArrayList<>();
        brokenAttributes = new ArrayList<>();
        genres = new HashSet<>();
    }

    public void run() {
        parseDocument();
        printData();
    }

    private void parseDocument() {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        InputSource inputSource = new InputSource(XML_FILE_PATH);
        inputSource.setEncoding(ENCODING);

        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(inputSource, this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            System.out.println("Error parsing SAX parser: " + e.getMessage());
        }
    }

    private void printData() {
        System.out.println("Number of movies found: " + validMovies.size());
        for (Movie movie : validMovies) {
            System.out.println("\t" + movie.toString());
        }

        System.out.println("Number of broken movies: " + brokenMovies.size());
        for (Movie movie : brokenMovies) {
            System.out.println("\t" + movie.toString());
        }

        System.out.println("Number of broken attributes: " + brokenAttributes.size());
        for (String attr : brokenAttributes) {
            System.out.println("\t" + attr);
        }

        System.out.println("Number of genres: " + genres.size());
        for (String genre : genres) {
            System.out.println("\t" + genre);
        }
    }

    private void resetTempValue() {
        tempValue = "";
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        resetTempValue();
        if (qName.equalsIgnoreCase(MOVIE_TAG)) {
            tempMovie = new Movie();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempValue = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        parseMovie(qName);
    }

    private void parseMovie(String qName) {
        if (qName.equalsIgnoreCase(MOVIE_TAG)) {
            addMovieToList(tempMovie);
        } else if (qName.equalsIgnoreCase(ID_TAG)) {
            tempMovie.setId(tempValue);
        } else if (qName.equalsIgnoreCase(TITLE_TAG)) {
            tempMovie.setTitle(tempValue);
        } else if (qName.equalsIgnoreCase(YEAR_TAG)) {
            tempMovie.setYear(parseYear(tempValue));
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

    private int parseYear(String year) {
        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            brokenAttributes.add("Error parsing movie: " + e.getMessage());
//            return Integer.parseInt(year.replaceAll("\\D", "0"));
            return -1;
        }
    }

    private void addMovieToList(Movie movie) {
        if (isValidMovie(movie)) {
            validMovies.add(movie);
        } else {
            brokenMovies.add(movie);
        }
    }

    private boolean isValidMovie(Movie movie) {
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
