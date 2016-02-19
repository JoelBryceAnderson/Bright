package joelbryceanderson.com.bright.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

import joelbryceanderson.com.bright.Fragments.AlarmsFragment;
import joelbryceanderson.com.bright.Fragments.GroupsFragment;
import joelbryceanderson.com.bright.Fragments.LightsFragment;
import joelbryceanderson.com.bright.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean lightsOff = true;
    private PHHueSDK phHueSDK;
    private Fragment fragment;
    private static final int MAX_HUE = 65535;
    public static final String TAG = "BrightApp";
    private FloatingActionButton fab;
    private Boolean darkMode;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        phHueSDK = PHHueSDK.create();
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayView(R.id.nav_lights);
        setFabAction(0);
        if (darkMode) {
            fab.setImageTintList(ColorStateList.valueOf(Color.BLACK));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId) {

        fragment = null;
        Intent intent = null;
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
                break;
            case R.id.nav_groups:
                fragment = new GroupsFragment();
                title = "Groups";
                fab.setImageResource(R.drawable.ic_plus);
                setFabAction(1);
                break;
            case R.id.nav_alarms:
                fragment = new AlarmsFragment();
                title = "Alarms";
                fab.setImageResource(R.drawable.ic_plus);
                setFabAction(2);
                break;
            case R.id.nav_bridge:
                intent = new Intent(this, MyBridgeActivity.class);
                break;
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_layout, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (intent != null) {
            startActivity(intent);
        }
        showFAB();
    }

    /**
     * Sets the brightness of all lights in a bridge.
     *
     * @param brightness value to be set (0-250)
     */
    public void setAllLightsBrightness(int brightness) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            if (brightness > 0) {
                lightState.setOn(true);
                lightState.setBrightness(brightness);
            } else {
                lightState.setOn(false);
            }
            bridge.updateLightState(light, lightState);
        }
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
     * @param position
     */
    public void setFabAction(int position) {
        if (position == 0) {
            fab.setOnClickListener(new View.OnClickListener() {
                //Button toggles lights in Light Fragment view
                @Override
                public void onClick(View view) {
                    if (lightsOff) {
                        lightsOff = false;
                        setAllLightsBrightness(250);
                        fab.setImageDrawable(getDrawable(R.drawable.ic_lightbulb_open));
                        Snackbar.make(view, "Turning all lights on", Snackbar.LENGTH_SHORT).show();
                        LightsFragment fragment = (LightsFragment)
                                getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                        fragment.toggleAll(true);
                    } else {
                        lightsOff = true;
                        setAllLightsBrightness(0);
                        fab.setImageDrawable(getDrawable(R.drawable.ic_lightbulb));
                        Snackbar.make(view, "Turning all lights off", Snackbar.LENGTH_SHORT).show();
                        LightsFragment fragment = (LightsFragment)
                                getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                        fragment.toggleAll(false);
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

    public Boolean isDarkMode() {
        return darkMode;
    }
}
