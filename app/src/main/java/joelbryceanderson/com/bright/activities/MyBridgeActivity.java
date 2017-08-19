package joelbryceanderson.com.bright.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;


import joelbryceanderson.com.bright.hue.HueSharedPreferences;
import joelbryceanderson.com.bright.hue.PHHomeActivity;
import joelbryceanderson.com.bright.R;

public class MyBridgeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Check if darkmode
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            setTheme(R.style.AppThemeNight);
        }

        //Initialize view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bridge);

        //Setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bridge);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Bridge");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up the hue bridge
        PHHueSDK sdk = PHHueSDK.getStoredSDKObject();
        final PHBridge mBridge = sdk.getSelectedBridge();

        //Set up views
        CardView cardView = (CardView) findViewById(R.id.my_bridge_card_view);
        ImageView bridgeImage = (ImageView) findViewById(R.id.bridge_image);
        TextView myBridgeText = (TextView) findViewById(R.id.my_bridge_text);

        //Set up all textviews
        TextView bridgeIDText = (TextView) findViewById(R.id.bridge_id_text);
        TextView modelIDText = (TextView) findViewById(R.id.model_id_text);
        TextView ipAddressText = (TextView) findViewById(R.id.ip_address_text);
        TextView macAddressText = (TextView) findViewById(R.id.mac_address_text);
        TextView gatewayText = (TextView) findViewById(R.id.gateway_text);
        TextView apiVersionText = (TextView) findViewById(R.id.api_version_text);
        TextView softwareVersionText = (TextView) findViewById(R.id.software_version_text);
        TextView zigbeeChannelText = (TextView) findViewById(R.id.zigbee_channel_text);

        //Get all info from bridge
        String bridgeID = mBridge.getResourceCache().getBridgeConfiguration().getBridgeID();
        String modelId = mBridge.getResourceCache().getBridgeConfiguration().getModelId();
        String ipAddress = mBridge.getResourceCache().getBridgeConfiguration().getIpAddress();
        String macAddress = mBridge.getResourceCache().getBridgeConfiguration().getMacAddress();
        String gateway = mBridge.getResourceCache().getBridgeConfiguration().getGateway();
        String apiVersion = mBridge.getResourceCache().getBridgeConfiguration().getAPIVersion();
        String softwareVersion = mBridge.getResourceCache()
                .getBridgeConfiguration().getSoftwareVersion();
        String zigbeeChannel = Integer
                .toString(mBridge.getResourceCache().getBridgeConfiguration().getZigbeeChannel());

        //Set textviews with info
        bridgeIDText.setText(bridgeIDText.getText().toString().concat(bridgeID));
        modelIDText.setText(modelIDText.getText().toString().concat(modelId));
        ipAddressText.setText(ipAddressText.getText().toString().concat(ipAddress));
        macAddressText.setText(macAddressText.getText().toString().concat(macAddress));
        gatewayText.setText(gatewayText.getText().toString().concat(gateway));
        apiVersionText.setText(apiVersionText.getText().toString().concat(apiVersion));
        softwareVersionText.setText(softwareVersionText
                .getText().toString().concat(softwareVersion));
        zigbeeChannelText.setText(zigbeeChannelText.getText().toString().concat(zigbeeChannel));

        //Change bridge image if using 2nd gen hue bridge
        if (modelId.equals("BSB002")) {
            bridgeImage.setImageResource(R.drawable.hue_bridge_2);
        }

        //Change text colors in darkmode
        if (darkMode) {
            myBridgeText.setTextColor(Color.parseColor("#ffffff"));
            bridgeIDText.setTextColor(Color.parseColor("#ffffff"));
            modelIDText.setTextColor(Color.parseColor("#ffffff"));
            ipAddressText.setTextColor(Color.parseColor("#ffffff"));
            macAddressText.setTextColor(Color.parseColor("#ffffff"));
            gatewayText.setTextColor(Color.parseColor("#ffffff"));
            ipAddressText.setTextColor(Color.parseColor("#ffffff"));
            apiVersionText.setTextColor(Color.parseColor("#ffffff"));
            softwareVersionText.setTextColor(Color.parseColor("#ffffff"));
            zigbeeChannelText.setTextColor(Color.parseColor("#ffffff"));
            cardView.setCardBackgroundColor(Color.parseColor("#263238"));
        }

        //Set up the Forget Bridge Button
        Button forgetBridgeButton = (Button) findViewById(R.id.forget_bridge_button);
        forgetBridgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder deleter = new
                        AlertDialog.Builder(MyBridgeActivity.this);
                deleter.setTitle("Forget Bridge");
                deleter.setMessage("Would you like to forget your Hue Bridge? " +
                        "This will also delete any groups you have created.");
                deleter.setPositiveButton("Forget", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        HueSharedPreferences prefs =
                                HueSharedPreferences.getInstance(getApplicationContext());
                        prefs.setLastConnectedIPAddress(null);
                        prefs.setUsername(null);
                        SharedPreferences appSharedPrefs = PreferenceManager
                                .getDefaultSharedPreferences(MyBridgeActivity.this);
                        appSharedPrefs.edit().putStringSet("groups", null).commit();
                        Intent i = new Intent(getApplicationContext(), PHHomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                });
                deleter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                deleter.show();
            }
        });

        //Set up the edit IP address button
        final ImageView editIp = (ImageView) findViewById(R.id.edit_ip_button);
        editIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder areYouSure =
                        new AlertDialog.Builder(MyBridgeActivity.this);
                areYouSure.setTitle("Edit IP?");
                areYouSure.setMessage("Editing the bridge IP address is for advanced users only." +
                        " Would you like to continue?");
                areYouSure.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Create dialog with text editor
                        final AlertDialog editIpDialog = new AlertDialog.Builder(
                                MyBridgeActivity.this).create();
                        final EditText input = new EditText(MyBridgeActivity.this);
                        input.setHint(mBridge.getResourceCache()
                                .getBridgeConfiguration().getIpAddress());
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                        editIpDialog.setTitle("Set IP Address");
                        editIpDialog.setView(input);
                        editIpDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                                "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final AlertDialog.Builder areYouSureAgain =
                                                new AlertDialog.Builder(MyBridgeActivity.this);
                                        areYouSureAgain.setTitle("Edit IP?");
                                        areYouSureAgain.setMessage("This will restart the " +
                                                "app with your new IP Address. Continue?");
                                        areYouSureAgain.setPositiveButton("Yes",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        String newIP = input.getText().toString();
                                                        HueSharedPreferences prefs = HueSharedPreferences.
                                                                getInstance(getApplicationContext());
                                                        prefs.setLastConnectedIPAddress(newIP);
                                                        Intent i = getBaseContext().getPackageManager()
                                                                .getLaunchIntentForPackage(
                                                                        getBaseContext()
                                                                                .getPackageName());
                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(i);
                                                    }
                                                });
                                        areYouSureAgain.setNegativeButton("No",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        areYouSureAgain.show();
                                    }
                                });
                        editIpDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                                "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editIpDialog.dismiss();
                            }
                        });
                        editIpDialog.show();
                    }
                });
                areYouSure.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                areYouSure.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
