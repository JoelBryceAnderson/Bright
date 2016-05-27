package com.JoelBryceAnderson.bright;

/**
 * Created by JAnderson on 2/14/16.
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
