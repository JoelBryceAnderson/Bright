package joelbryceanderson.com.bright.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import joelbryceanderson.com.bright.Adapters.GroupPickerAdapter;
import joelbryceanderson.com.bright.LightGroup;
import joelbryceanderson.com.bright.R;

public class GroupPickerActivity extends AppCompatActivity {

    private GroupPickerAdapter adapter;
    private EditText editText;
    private FloatingActionButton fab;
    private GoogleApiClient mGoogleApiClient;
    private PHHueSDK phHueSDK;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Check if app is in dark mode, switch theme if it is
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            setTheme(R.style.AppThemeNight);
        }

        //Set up the views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_picker);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
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

        phHueSDK = PHHueSDK.getInstance();

        //Set up the Action Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_group);
        fab = (FloatingActionButton) findViewById(R.id.fab_done);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create a Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);


        editText = (EditText) findViewById(R.id.enter_group_name);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!adapter.getListToReturn().isEmpty() &&
                        !editText.getText().toString().matches("")) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Initialize recycler view
        recyclerView = (RecyclerView) findViewById(R.id.group_picker_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        //Get all lights on the Hue Bridge, put them into the recycler view
        PHHueSDK phHueSDK = PHHueSDK.getInstance();
        PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
        List<PHLight> myLights = cache.getAllLights();
        adapter = new GroupPickerAdapter(myLights, true);
        recyclerView.setAdapter(adapter);

        //Hide the FAB on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    hideFab();
                } else {
                    showFab();
                }
            }
        });

        //Handle FAB click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        //Adjust View colors for dark mode
        if (darkMode) {
            fab.setImageTintList(ColorStateList.valueOf(Color.BLACK));
        }

        fab.hide();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    @Override
    public boolean onSupportNavigateUp() {
        //Finish activity if close button is pressed
        finish();
        return false;
    }

    public void createGroup() {
        if (!adapter.getListToReturn().isEmpty() && !editText.getText().toString().matches("")) {
            PHGroup newGroup = new PHGroup();
            List <String> lightIdentifiers = new ArrayList<>();

            for (PHLight light : adapter.getListToReturn()) {
                lightIdentifiers.add(light.getIdentifier());
            }

            newGroup.setLightIdentifiers(lightIdentifiers);

            phHueSDK.getSelectedBridge().createGroup(newGroup, new PHGroupListener() {
                @Override
                public void onCreated(PHGroup phGroup) {
                    //Get all selected lights
                    LightGroup group = new LightGroup(
                            adapter.getListToReturn(), editText.getText().toString(),
                            phGroup.getIdentifier());

                    //Put new group of selected lights into shared preferences as GSON object
                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(group);
                    Set<String> set = appSharedPrefs.getStringSet("myGroups", new HashSet<String>());
                    set.add(group.getName());
                    prefsEditor.putStringSet("myGroups", set);
                    prefsEditor.putString(group.getName(), json);
                    prefsEditor.apply();

                    syncDataItem(group.getName(), group.hasAnyColor());
                    finish();
                }

                @Override
                public void onReceivingGroupDetails(PHGroup phGroup) {

                }

                @Override
                public void onReceivingAllGroups(List<PHBridgeResource> list) {

                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(int i, String s) {
                    Snackbar.make(recyclerView,
                            "Unable to create group",
                            Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                }
            });
        } else if (!adapter.getListToReturn().isEmpty()) {
            //If group is not named, prompt user to name it
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Please name your group.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        } else {
            //If no lights are selected, prompt user to select them
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Please select at least one light.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public void hideFab() {
        fab.hide();
    }

    public void showFab() {
        if (!adapter.getListToReturn().isEmpty() && !editText.getText().toString().matches("")) {
            fab.show();
        }
    }

    private void syncDataItem(String name, boolean hasColor) {
        if(mGoogleApiClient != null) {
            final PutDataMapRequest putRequest = PutDataMapRequest.create("/GROUPS");
            final DataMap map = putRequest.getDataMap();
            map.putString("name", name);
            map.putBoolean("hasColor", hasColor);
            Wearable.DataApi.putDataItem(mGoogleApiClient,
                    putRequest.asPutDataRequest().setUrgent()).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    Log.e("HEY!", dataItemResult.toString());
                }
            });
        } else {
            Log.e("HEY!", "HEY!");
        }
    }
}
