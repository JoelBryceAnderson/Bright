package joelbryceanderson.com.bright;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.Map;

/**
 * Created by JAnderson on 5/21/16.
 *
 * Used to store Scenes with multiple light states.
 * applyScene function to set states for each light in the scene.
 */
public class LightScene {

    private Map<PHLight, PHLightState> stateList;

    public LightScene(Map<PHLight, PHLightState> stateList) {
        this.stateList = stateList;
    }

    public void applyScene(PHBridge bridge) {
        for (PHLight light : stateList.keySet()) {
            bridge.updateLightState(light, stateList.get(light));
        }
    }
}
