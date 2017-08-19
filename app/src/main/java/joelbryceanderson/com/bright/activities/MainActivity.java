package joelbryceanderson.com.bright.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLightState;

import joelbryceanderson.com.bright.fragments.AlarmsFragment;
import joelbryceanderson.com.bright.fragments.GroupsFragment;
import joelbryceanderson.com.bright.fragments.LightsFragment;
import joelbryceanderson.com.bright.hue.PHHomeActivity;
import joelbryceanderson.com.bright.R;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private boolean lightsOff = true;
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE = 65535;
    public static final String TAG = "BrightApp";
    private FloatingActionButton fab;
    private Boolean darkMode;
    private int currentlySelected;
    private BottomNavigationView mNavigationView;
    private Toolbar toolbar;
    private Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            setTheme(R.style.AppThemeNight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        phHueSDK = PHHueSDK.create();
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setOnNavigationItemSelectedListener(this);

        boolean groupsOnStartup = prefs.getBoolean("groups_on_startup", false);
        if (groupsOnStartup) {
            currentlySelected = 1;
        } else {
            currentlySelected = 0;
        }

        if (savedInstanceState != null) {
            currentlySelected = savedInstanceState.getInt("currentFrag");
        }

        setFabAction(currentlySelected);
        displayView(mNavigationView.getMenu().getItem(currentlySelected).getItemId());
        mNavigationView.getMenu().getItem(currentlySelected).setChecked(true);

        if (darkMode) {
            fab.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            mNavigationView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryNight));
            mNavigationView.setItemTextColor(ContextCompat.getColorStateList(MainActivity.this, R.color.White));
            mNavigationView.setItemIconTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.White));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFrag", currentlySelected);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId) {
        String title = getString(R.string.app_name);
        switch (viewId) {
            case R.id.nav_lights:
                fragment = new LightsFragment();
                title  = "Lights";
                setFabAction(0);
                if (lightsOff) {
                    fab.setImageResource(R.drawable.ic_lightbulb);
                } else {
                    fab.setImageResource(R.drawable.ic_lightbulb_open);
                }
                currentlySelected = 0;
                break;
            case R.id.nav_groups:
                fragment = new GroupsFragment();
                title = "Groups";
                fab.setImageResource(R.drawable.ic_plus);
                setFabAction(1);
                currentlySelected = 1;
                break;
            case R.id.nav_alarms:
                fragment = new AlarmsFragment();
                title = "Alarms";
                fab.setImageResource(R.drawable.ic_plus);
                setFabAction(2);
                currentlySelected = 2;
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.frame_layout, fragment);
            ft.commit();
        }
        if (getSupportActionBar() != null && fragment != null) {
            getSupportActionBar().setTitle(title);
        }
        showFAB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_item:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets the brightness of all lights in a bridge.
     *
     * @param brightness value to be set (0-250)
     */
    public void setAllLightsBrightness(int brightness) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        if (brightness > 0) {
            lightState.setOn(true);
            lightState.setBrightness(brightness);
        } else {
            lightState.setOn(false);
        }

        bridge.setLightStateForDefaultGroup(lightState);
    }

    /**
     * Hides the floating action button
     */
    public void hideFAB() {
        fab.hide();
    }

    /**
     * Shows the floating action button
     */
    public void showFAB() {
        fab.show();
    }

    /**
     * Gets the currently connected philips hue bridge
     * @return the current bridge
     */
    public PHBridge getBridge() {
        return phHueSDK.getSelectedBridge();
    }

    /**
     * Handles the floating action button's action when fragments switch
     * @param position position in the fab drawer to decide fab functionality
     */
    public void setFabAction(int position) {
        if (position == 0) {
            fab.setOnClickListener(new View.OnClickListener() {
                //Button toggles lights in Light Fragment view
                @Override
                public void onClick(View view) {
                    if (lightsOff) {
                        fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                            @Override
                            public void onHidden(FloatingActionButton fab) {
                                super.onHidden(fab);
                                lightsOff = false;
                                setAllLightsBrightness(250);
                                fab.setImageDrawable(getDrawable(R.drawable.ic_lightbulb_open));
                                LightsFragment fragment = (LightsFragment)
                                        getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                                fragment.toggleAll(true);
                                fab.show();
                            }
                        });
                    } else {
                        fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                            @Override
                            public void onHidden(FloatingActionButton fab) {
                                super.onHidden(fab);
                                lightsOff = true;
                                setAllLightsBrightness(0);
                                fab.setImageDrawable(getDrawable(R.drawable.ic_lightbulb));
                                LightsFragment fragment = (LightsFragment)
                                        getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                                fragment.toggleAll(false);
                                fab.show();
                            }
                        });
                    }
                }
            });
        } else if (position == 1) {
            //Button adds group in Groups Fragment view
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupsFragment fragment = (GroupsFragment)
                            getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                    fragment.addNewGroup();
                }
            });
        } else if (position == 2) {
            //Button adds alarm in Alarms Fragment view
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmsFragment fragment = (AlarmsFragment)
                            getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                    fragment.addNewAlarm();
                }
            });
        }
    }

    public void setFabTogglesOff() {
        lightsOff = false;
        fab.setImageDrawable(getDrawable(R.drawable.ic_lightbulb_open));
    }

    public void restartSplashActivity() {
        Intent intent = new Intent(getApplicationContext(), PHHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }
}
