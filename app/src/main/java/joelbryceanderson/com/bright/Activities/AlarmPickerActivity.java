package joelbryceanderson.com.bright.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import joelbryceanderson.com.bright.Adapters.GroupPickerAdapter;
import joelbryceanderson.com.bright.R;

public class AlarmPickerActivity extends AppCompatActivity {

    private GroupPickerAdapter adapter;
    private FloatingActionButton fab;
    private int currentColor;
    private Boolean darkMode;
    private ImageView colorSelector;
    private Switch turnLightsOnOff;
    private Switch switchRepeating;
    private int hours;
    private int minutes;
    private int mBitWise;
    private boolean[] daysSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Check if app is in dark mode, switch theme if it is
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            setTheme(R.style.AppThemeNight);
        }

        //Set up the views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_picker);

        Intent intent = getIntent();
        hours = intent.getIntExtra("selectedHour", 0);
        minutes = intent.getIntExtra("selectedMinute", 0);


        //Set up the Action Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_alarm_picker);
        fab = (FloatingActionButton) findViewById(R.id.fab_done_alarm);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create an Alarm");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        turnLightsOnOff = (Switch) findViewById(R.id.turn_lights_on_switch_alarm_picker);
        switchRepeating = (Switch) findViewById(R.id.switch_repeating);
        final LinearLayout daysLayout = (LinearLayout) findViewById(R.id.days_of_week_layout);

        switchRepeating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    daysLayout.setVisibility(View.VISIBLE);
                } else {
                    daysLayout.setVisibility(View.GONE);
                }
            }
        });

        //Initialize the color selector
        colorSelector = (ImageView) findViewById(R.id.circle_color_alarm_picker);
        colorSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (turnLightsOnOff.isChecked()) {
                    createDialog();
                }
            }
        });

        //Handle the days of the week
        daysSelected = new boolean[7];
        for (int i = 0; i < 7; ++i) {
            final int pos = i;
            daysLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (daysSelected[pos]) {
                        daysSelected[pos] = false;
                        ((TextView) daysLayout.getChildAt(pos)).setTextColor(Color.parseColor("#727272"));
                    } else {
                        daysSelected[pos] = true;
                        ((TextView) daysLayout.getChildAt(pos)).setTextColor(Color.parseColor("#0288D1"));
                    }
                }
            });
        }

        //Initialize the turn lights on/off switch
        turnLightsOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    turnLightsOnOff.setText("Turn lights on");
                    colorSelector.setColorFilter(currentColor);
                } else {
                    colorSelector.setColorFilter(Color.parseColor("#000000"));
                    turnLightsOnOff.setText("Turn lights off");
                }
            }
        });

        //Initialize recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.alarm_picker_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        //Get all lights on the Hue Bridge, put them into the recycler view
        PHHueSDK phHueSDK = PHHueSDK.getInstance();
        PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
        List<PHLight> myLights = cache.getAllLights();
        adapter = new GroupPickerAdapter(myLights, phHueSDK.getSelectedBridge(), false);
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
                createAlarm();
            }
        });

        //Adjust View colors for dark mode
        if (darkMode) {
            fab.setImageTintList(ColorStateList.valueOf(Color.BLACK));
        }

        hideFab();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //Finish activity if close button is pressed
        finish();
        return false;
    }

    private void createAlarm() {
        //Initialize bridge and schedule
        createBitwise();
        PHHueSDK phHueSDK = PHHueSDK.create();
        final PHBridge mBridge = phHueSDK.getSelectedBridge();
        final String nameMaker = "name" + Integer.toString(hours)
                + Integer.toString(minutes);
        final PHSchedule schedule = new PHSchedule(nameMaker);

        //Build light state
        PHLightState state = new PHLightState();
        state.setOn(turnLightsOnOff.isChecked());
        if (turnLightsOnOff.isChecked()) {
            float xy[] = PHUtilities.calculateXY(currentColor, "LCT001");
            state.setX(xy[0]);
            state.setY(xy[1]);
            state.setBrightness(255);
        }
        schedule.setLightState(state);
        schedule.setStatus(PHSchedule.PHScheduleStatus.ENABLED);
        schedule.setStartTime(new Date());

        //Set repeating days
        if (!switchRepeating.isChecked()) {
            schedule.setRecurringDays(PHSchedule.RecurringDay.RECURRING_NONE.getValue());
        } else {
            schedule.setRecurringDays(mBitWise);
        }


        //Create group, set group identifier
        PHGroup group = new PHGroup();
        List<PHLight> phLightList = adapter.getListToReturn();
        List<String> lightIdentifiers = new ArrayList<>();
        for (PHLight light : phLightList) {
            lightIdentifiers.add(light.getIdentifier());
        }
        group.setLightIdentifiers(lightIdentifiers);
        group.setIdentifier(nameMaker);
        mBridge.createGroup(group, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {
                schedule.setGroupIdentifier(phGroup.getIdentifier());
                schedule.setIdentifier(nameMaker);

                //Set Time of alarm
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, minutes);
                cal.set(Calendar.HOUR_OF_DAY, hours);
                schedule.setLocalTime(true);
                Calendar checker = Calendar.getInstance();
                if (cal.before(checker)) {
                    cal.add(Calendar.DATE, 1);
                }
                schedule.setDate(cal.getTime());

                mBridge.updateSchedule(schedule, new PHScheduleListener() {
                    @Override
                    public void onCreated(PHSchedule phSchedule) {
                    }

                    @Override
                    public void onSuccess() {
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }

                    @Override
                    public void onError(int i, String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                    }
                });
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

            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
    }

    public void hideFab() {
        fab.hide();
    }

    public void showFab() {
        if (!adapter.getListToReturn().isEmpty()) {
            fab.show();
        }
    }

    public int createBitwise() {
        mBitWise = 0;
        if (daysSelected[0]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_SUNDAY.getValue();
        }
        if (daysSelected[1]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_MONDAY.getValue();
        }
        if (daysSelected[2]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_TUESDAY.getValue();
        }
        if (daysSelected[3]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_WEDNESDAY.getValue();
        }
        if (daysSelected[4]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_THURSDAY.getValue();
        }
        if (daysSelected[5]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_FRIDAY.getValue();
        }
        if (daysSelected[6]) {
            mBitWise += PHSchedule.RecurringDay.RECURRING_SATURDAY.getValue();
        }
        return mBitWise;
    }

    public void createDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_picker_sliders);

        final LinearLayout linearLayout = (LinearLayout)
                dialog.findViewById(R.id.color_picker_background);
        if (darkMode) {
            linearLayout.setBackgroundColor(Color.parseColor("#263238"));
        }
        final ImageView color = (ImageView) dialog.findViewById(R.id.color);
        final ImageView colorSpectrum = (ImageView) dialog.findViewById(R.id.color_spectrum);

        ImageView presetOne = (ImageView) dialog.findViewById(R.id.preset_one);
        ImageView presetTwo = (ImageView) dialog.findViewById(R.id.preset_two);
        ImageView presetThree = (ImageView) dialog.findViewById(R.id.preset_three);
        ImageView presetFour = (ImageView) dialog.findViewById(R.id.preset_four);

        presetOne.setColorFilter(PHUtilities.colorFromXY(new float[]{0.5134f, 0.4149f}, "LCT001"));
        presetTwo.setColorFilter(PHUtilities.colorFromXY(new float[]{0.4596f, 0.4105f}, "LCT001"));
        presetThree.setColorFilter(PHUtilities.colorFromXY(new float[]{0.4449f, 0.4066f}, "LCT001"));
        presetFour.setColorFilter(PHUtilities.colorFromXY(new float[]{0.3693f, 0.3695f}, "LCT001"));

        presetOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentColor = PHUtilities.colorFromXY(new float[]{0.5134f, 0.4149f}, "LCT001");
                colorSelector.setColorFilter(currentColor);
                dialog.cancel();
            }
        });

        presetTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentColor = PHUtilities.colorFromXY(new float[]{0.4596f, 0.4105f}, "LCT001");
                colorSelector.setColorFilter(currentColor);
                dialog.cancel();
            }
        });


        presetThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentColor = PHUtilities.colorFromXY(new float[]{0.4449f, 0.4066f}, "LCT001");
                colorSelector.setColorFilter(currentColor);
                dialog.cancel();
            }
        });


        presetFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentColor = PHUtilities.colorFromXY(new float[]{0.3693f, 0.3695f}, "LCT001");
                colorSelector.setColorFilter(currentColor);
                dialog.cancel();
            }
        });

        final Bitmap bitmap = ((BitmapDrawable) colorSpectrum.getDrawable()).getBitmap();

        colorSpectrum.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        Matrix inverse = new Matrix();
                        colorSpectrum.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[]{event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);
                        int currentX = Integer.valueOf((int) touchPoint[0]);
                        int currentY = Integer.valueOf((int) touchPoint[1]);

                        if (currentX < 0) {
                            currentX = 0;
                        }
                        if (currentX > bitmap.getWidth() - 1) {
                            currentX = bitmap.getWidth() - 1;
                        }

                        if (currentY < 0) {
                            currentY = 0;
                        }
                        if (currentY > bitmap.getHeight() - 1) {
                            currentY = bitmap.getHeight() - 1;
                        }

                        int pixel = bitmap.getPixel(currentX, currentY);
                        currentColor = Color.argb(255,
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                        color.setColorFilter(currentColor);
                }
                return true;
            }
        });

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSelector.setColorFilter(currentColor);
                dialog.cancel();
            }
        });
        dialog.show();
    }
}
