package joelbryceanderson.com.bright.Fragments;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import joelbryceanderson.com.bright.Activities.AlarmPickerActivity;
import joelbryceanderson.com.bright.Activities.MainActivity;
import joelbryceanderson.com.bright.Adapters.RecyclerViewAdapterAlarms;
import joelbryceanderson.com.bright.Adapters.RecyclerViewAdapterGroups;
import joelbryceanderson.com.bright.Adapters.RecyclerViewAdapterLights;
import joelbryceanderson.com.bright.LightGroup;
import joelbryceanderson.com.bright.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmsFragment extends android.support.v4.app.Fragment {

    private RecyclerView recyclerView;
    private FrameLayout frameLayout;
    private List<PHSchedule> scheduleList;
    private RecyclerViewAdapterAlarms adapter;
    private PHHueSDK phHueSDK;
    private CardView noItemsCard;


    public AlarmsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarms, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = (RecyclerView) getView().findViewById(R.id.alarms_recycler);
        frameLayout = (FrameLayout) getView().findViewById(R.id.alarms_frame_layout);
        noItemsCard = (CardView) getView().findViewById(R.id.no_alarms_card);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        phHueSDK = PHHueSDK.getInstance();
        if (phHueSDK.getSelectedBridge() == null) {
            ((MainActivity)getActivity()).restartSplashActivity();
        }
        PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
        scheduleList = new ArrayList<>(cache.getSchedules().values());
        MainActivity parent = (MainActivity) getActivity();
        adapter = new RecyclerViewAdapterAlarms(scheduleList, parent.getBridge(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    ((MainActivity) getActivity()).hideFAB();
                } else {
                    ((MainActivity) getActivity()).showFAB();
                }
            }
        });
        if (!scheduleList.isEmpty()) {
            noItemsCard.setVisibility(View.GONE);
        }
        List<PHGroup> groups = cache.getAllGroups();
        List<String> identifiersInUse = new ArrayList<>();
        for (PHSchedule schedule : scheduleList) {
            identifiersInUse.add(schedule.getGroupIdentifier());
        }
        for (PHGroup group : groups) {
            if (!identifiersInUse.contains(group.getIdentifier()) && group.getIdentifier().startsWith("name")) {
                phHueSDK.getSelectedBridge().deleteGroup(group.getIdentifier(), null);
            }
        }
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (prefs.getBoolean("dark_mode", false)) {
            noItemsCard.setCardBackgroundColor(Color.parseColor("#263238"));
            TextView mainText = (TextView) getView().findViewById(R.id.no_alarms_card_text);
            TextView subText = (TextView) getView().findViewById(R.id.no_alarms_card_subtext);
            mainText.setTextColor(Color.parseColor("#ffffff"));
            subText.setTextColor(Color.parseColor("#ffffff"));
        }
    }

    public void addNewAlarm() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        final TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Intent intent = new Intent(getContext(), AlarmPickerActivity.class);
                intent.putExtra("selectedHour", selectedHour);
                intent.putExtra("selectedMinute", selectedMinute);
                startActivityForResult(intent, 200);
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @Override
    public void onActivityResult(int arg1, int arg2, Intent data)
    {
        if (arg2 == Activity.RESULT_OK) {
            phHueSDK = PHHueSDK.getInstance();
            PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
            scheduleList = new ArrayList<>(cache.getSchedules().values());
            MainActivity parent = (MainActivity) getActivity();
            adapter = new RecyclerViewAdapterAlarms(scheduleList, parent.getBridge(), this);
            recyclerView.setAdapter(adapter);
            noItemsCard.setVisibility(View.GONE);
        }
    }

    public void deleteAlarm(final int position) {
        final PHSchedule remove = scheduleList.remove(position);
        adapter.notifyItemRemoved(position);
        phHueSDK.getSelectedBridge().removeSchedule(remove.getIdentifier(), new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {
                Snackbar snackbar = Snackbar.make(frameLayout, "Alarm deleted",
                        Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar snackbar1 = Snackbar.make(
                                frameLayout, "Alarm restored", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        scheduleList.add(position, remove);
                        adapter.notifyItemInserted(position);
                        phHueSDK.getSelectedBridge().updateSchedule(remove, new PHScheduleListener() {
                            @Override
                            public void onCreated(PHSchedule phSchedule) {

                            }

                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(int i, String s) {
                                Snackbar snackbar2 = Snackbar.make(
                                        frameLayout, "Unable to restore Alarm", Snackbar.LENGTH_SHORT);
                                snackbar2.show();
                            }

                            @Override
                            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                            }
                        });
                    }
                });
                snackbar.show();
            }

            @Override
            public void onError(int i, String s) {
                Snackbar snackbar1 = Snackbar.make(
                        frameLayout, "Unable to delete Alarm", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
        if (recyclerView.getChildCount() == 0) {
            noItemsCard.setVisibility(View.VISIBLE);
        }
    }
}
