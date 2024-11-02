import java.util.HashSet;

public class Movie implements DataBaseItem {
    private String id;
    private String title;
    private int year;
    private String director;
    private HashSet<String> genres = new HashSet<>();

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public Movie() {
        year = -1;
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

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty()
                && title != null && !title.isEmpty()
                && year != -1
                && director != null && !director.isEmpty();
    }

    @Override
    public String toString() {
        return "Id: " + id + ", Title: " + title + ", Year: " + year + ", Director: " + director;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Movie) {
            Movie other = (Movie) obj;
            return id.equals(other.id);
        }
        return false;
    }
}
