import java.util.HashSet;
import java.util.Objects;

public class Movie implements DataBaseItem {
    private String id;
    private String title;
    private int year;
    private String director;
    private final HashSet<String> genres = new HashSet<>();
    private final HashSet<Star> movieStars = new HashSet<>();

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public Movie() {
        year = FabflixSAXParser.invalidIntValue;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public HashSet<String> getGenres() {
        return genres;
    }

    public HashSet<Star> getMovieStars() {
        return movieStars;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void addGenre(String genre) {
        genres.add(genre);
    }

    public void addMovieStar(Star movieStar) {
        movieStars.add(movieStar);
    }

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty()
                && title != null && !title.isEmpty()
                && year != FabflixSAXParser.invalidIntValue
                && director != null && !director.isEmpty();
    }

    @Override
    public String toString() {
        return "Id: " + id + ", Title: " + title + ", Year: " + year + ", Director: " + director
                + "\n\t\tMovie Stars: " + movieStars;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Movie) {
            Movie other = (Movie) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
