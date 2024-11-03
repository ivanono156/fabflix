import java.util.Objects;

public class StarInMovie implements DataBaseItem {
    private String movieId;
    private String starId;

    private Movie movie;
    private Star star;

    public StarInMovie(String movieId, String starId) {
        this.movieId = movieId;
        this.starId = starId;
    }

    public StarInMovie() {

    }

    public String getMovieId() {
        return movieId;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getStarId() {
        return starId;
    }

    public Star getStar() {
        return star;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    public void setStar(Star star) {
        this.star = star;
    }

    @Override
    public void setId(String id) {
        // Ignored
    }

    @Override
    public String getId() {
        return getMovieId() + "," + getStarId();
    }

    @Override
    public boolean isValid() {
        return starId != null && !starId.isEmpty() && movieId != null && !movieId.isEmpty();
    }

    @Override
    public String toString() {
        return "Movie ID: " + movieId + ", Star ID: " + starId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StarInMovie) {
            StarInMovie other = (StarInMovie) obj;
            return movieId.equals(other.movieId) && starId.equals(other.starId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, starId);
    }
}
