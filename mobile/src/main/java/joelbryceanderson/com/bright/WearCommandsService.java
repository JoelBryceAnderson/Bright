package joelbryceanderson.com.bright;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import joelbryceanderson.com.bright.Hue.HueSharedPreferences;

/**
 * Created by JAnderson on 5/16/16.
 */
public class WearCommandsService extends WearableListenerService {
    PHHueSDK phHueSDK;

    @Override
    public void onCreate() {
        super.onCreate();

        phHueSDK = PHHueSDK.create();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        PHBridge bridge = phHueSDK.getSelectedBridge();

        if (messageEvent.getPath().endsWith("on")) {
            String groupNameToChange = new String(messageEvent.getData());
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            String json = appSharedPrefs.getString(groupNameToChange, "");
            LightGroup group = gson.fromJson(json, LightGroup.class);

            PHLightState lightState = new PHLightState();
            lightState.setOn(true);
            bridge.setLightStateForGroup(group.getIdentifier(), lightState);
        } else if (messageEvent.getPath().endsWith("off")) {
            String groupNameToChange = new String(messageEvent.getData());
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            String json = appSharedPrefs.getString(groupNameToChange, "");
            LightGroup group = gson.fromJson(json, LightGroup.class);

            PHLightState lightState = new PHLightState();
            lightState.setOn(false);
            bridge.setLightStateForGroup(group.getIdentifier(), lightState);
        } else if (messageEvent.getPath().endsWith("brightness")) {
            String payload = new String(messageEvent.getData());
            String groupNameToChange = payload.substring(0, payload.indexOf("`"));
            String brightnessString = payload.substring(payload.indexOf("`") + 1);
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            String json = appSharedPrefs.getString(groupNameToChange, "");
            LightGroup group = gson.fromJson(json, LightGroup.class);

            int brightnessToSet = Integer.parseInt(brightnessString);

            PHLightState lightState = new PHLightState();
            lightState.setBrightness(brightnessToSet);
            lightState.setOn(true);
            bridge.setLightStateForGroup(group.getIdentifier(), lightState);
        } else if (messageEvent.getPath().endsWith("color")) {
            String payload = new String(messageEvent.getData());
            String groupNameToChange = payload.substring(0, payload.indexOf("`"));
            String colorString = payload.substring(payload.indexOf("`") + 1);
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            String json = appSharedPrefs.getString(groupNameToChange, "");
            LightGroup group = gson.fromJson(json, LightGroup.class);

            int colorToSet = Integer.parseInt(colorString);

            for (PHLight light : group.getLights()) {
                if (light.supportsColor()) {
                    PHLightState lightState = new PHLightState();
                    float[] xy = PHUtilities.calculateXY(colorToSet, light.getModelNumber());
                    lightState.setX(xy[0]);
                    lightState.setY(xy[1]);
                    lightState.setOn(true);
                    bridge.updateLightState(light, lightState);
                }
            }
        } else if (messageEvent.getPath().endsWith("connectBridge")) {
            HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());
            String lastIpAddress = prefs.getLastConnectedIPAddress();
            String lastUsername = prefs.getUsername();

            // Automatically try to connect to the last connected IP Address.  For multiple bridge support a different implementation is required.
            if (lastIpAddress !=null && !lastIpAddress.equals("")) {
                PHAccessPoint lastAccessPoint = new PHAccessPoint();
                lastAccessPoint.setIpAddress(lastIpAddress);
                lastAccessPoint.setUsername(lastUsername);

                if (!phHueSDK.isAccessPointConnected(lastAccessPoint)) {
                    phHueSDK.connect(lastAccessPoint);
                }
            }
        }
    }
}
