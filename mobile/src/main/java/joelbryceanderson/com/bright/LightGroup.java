package joelbryceanderson.com.bright;

import com.philips.lighting.model.PHLight;

import java.util.List;

/**
 * Created by Joel Anderson on 2/14/16.
 * Model Class for groups of lights.
 */
public class LightGroup {

    private List<PHLight> lights;
    private String name;
    private String identifier;

    public LightGroup(List<PHLight> lights, String name, String identifier) {
        this.lights = lights;
        this.name = name;
        this.identifier = identifier;
    }

    public LightGroup(List<PHLight> lights) {
        this.lights = lights;
        this.name = "Unnamed";
    }

    public List<PHLight> getLights() {
        return lights;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setLights(List<PHLight> lights) {
        this.lights = lights;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdentifier(String name) {
        this.identifier = name;
    }

    public boolean hasAnyColor() {
        boolean hasColor = false;
        for (PHLight light : lights) {
            if (light.supportsColor()) {
                hasColor = true;
            }
        }
        return hasColor;
    }
}
