package joelbryceanderson.com.bright.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLightState;

import joelbryceanderson.com.bright.Fragments.AlarmsFragment;
import joelbryceanderson.com.bright.Fragments.GroupsFragment;
import joelbryceanderson.com.bright.Fragments.LightsFragment;
import joelbryceanderson.com.bright.Hue.PHHomeActivity;
import joelbryceanderson.com.bright.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean lightsOff = true;
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE = 65535;
    public static final String TAG = "BrightApp";
    private FloatingActionButton fab;
    private Boolean darkMode;
    private int currentlySelected;
    private NavigationView navigationView;
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        phHueSDK = PHHueSDK.create();
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        displayView(navigationView.getMenu().getItem(currentlySelected).getItemId());
        navigationView.getMenu().getItem(currentlySelected).setChecked(true);
        initDarkModeToggle();

        if (darkMode) {
            fab.setImageTintList(ColorStateList.valueOf(Color.BLACK));
            ImageView navHeaderImage = (ImageView) navigationView
                    .getHeaderView(0).findViewById(R.id.nav_header_image);
            navHeaderImage.setImageResource(R.drawable.nav_header_dark);

            ImageView navHeaderIcon = (ImageView) navigationView
                    .getHeaderView(0).findViewById(R.id.nav_header_icon);
            //navHeaderIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryNight));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFrag", currentlySelected);
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
            case R.id.nav_my_bridge:
                intent = new Intent(this, MyBridgeActivity.class);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_layout, fragment);
            ft.commit();
            if (toolbar.getMenu() != null) {
                toolbar.getMenu().clear();
            }
        }
        if (getSupportActionBar() != null && fragment != null) {
            getSupportActionBar().setTitle(title);
        }

        if (fragment != null || intent != null) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (intent != null) {
            startActivity(intent);
        }
        showFAB();
    }

    public void showSyncMenu() {
        toolbar.inflateMenu(R.menu.menu_groups);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync_groups_wear:
                if (fragment instanceof GroupsFragment) {
                    ((GroupsFragment) fragment).resync();
                }
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

    public Boolean isDarkMode() {
        return darkMode;
    }

    public void restartSplashActivity() {
        Intent intent = new Intent(getApplicationContext(), PHHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

    private void initDarkModeToggle() {
        View darkModeAction = navigationView
                .getMenu().findItem(R.id.nav_dark_mode).getActionView();

        Switch darkModeSwitch = (Switch) darkModeAction.findViewById(R.id.dark_mode_switch);
        darkModeSwitch.setChecked(darkMode);
        darkModeSwitch.setOnCheckedChangeListener(darkModeSwitch());
    }

    private CompoundButton.OnCheckedChangeListener darkModeSwitch() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                drawer.addDrawerListener(enableDarkMode(b));
            }
        };
    }

    private DrawerLayout.DrawerListener enableDarkMode(final boolean b) {
        return new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                toggleDarkMode(b);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };
    }

    private void toggleDarkMode(boolean b) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("dark_mode", b).apply();
        restartApp();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void restartApp() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
