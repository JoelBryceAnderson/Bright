package joelbryceanderson.com.bright;

import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

/**
 * Created by JAnderson on 2/14/16.
 */
public class LightGroup {

    private List<PHLight> lights;
    private String name;

    public LightGroup(List<PHLight> lights, String name) {
        this.lights = lights;
        this.name = name;
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

    public void setLights(List<PHLight> lights) {
        this.lights = lights;
    }

    public void setName(String name) {
        this.name = name;
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
