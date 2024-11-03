import java.util.Objects;

public class Star implements DataBaseItem{
    private String id;
    private String name;
    // Year can be null in database
    private Integer birthYear;

    public Star(String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public Star() {

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    @Override
    public boolean isValid() {
        return id != null && !id.isEmpty()
                && name != null && !name.isEmpty();
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Birth Year: " + birthYear;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Star) {
            Star other = (Star) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
