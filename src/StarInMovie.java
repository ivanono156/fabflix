public class StarInMovie implements DataBaseItem {
    private String movieId;
    private String starId;

    public StarInMovie(String movieId, String starId) {
        this.movieId = movieId;
        this.starId = starId;
    }

    public StarInMovie() {

    }

    public String getMovieId() {
        return movieId;
    }

    public String getStarId() {
        return starId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    @Override
    public void setId(String id) {
        // Ignored
    }

    @Override
    public String getId() {
        return "";  // Ignored
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
}
