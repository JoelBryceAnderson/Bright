package joelbryceanderson.com.bright;

/**
 * Created by Joel Anderson on 2/14/16.
 * Model class for light groups.
 */
public class Group {

    private String name;
    private boolean hasColor;

    public Group(String name, boolean hasColor) {
        this.name = name;
        this.hasColor = hasColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasAnyColor() {
        return hasColor;
    }

    public void setHasColor(boolean hasColor) {
        this.hasColor = hasColor;
    }
}
