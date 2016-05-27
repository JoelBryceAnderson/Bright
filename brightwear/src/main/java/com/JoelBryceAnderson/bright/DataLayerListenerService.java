package com.JoelBryceAnderson.bright;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by JAnderson on 5/16/16.
 */
public class DataLayerListenerService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if("/GROUPS".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                //Get info from data item from phone
                String name = map.getString("name");
                Boolean hasColor = map.getBoolean("hasColor");

                //create new group
                Group newGroup = new Group(name, hasColor);

                //Add group to shared preferences for activity use
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(this.getApplicationContext());
                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(newGroup);
                Set<String> set = appSharedPrefs.getStringSet("groups", new HashSet<String>());
                set.add(newGroup.getName());
                prefsEditor.putStringSet("groups", set);
                prefsEditor.putString(newGroup.getName(), json);
                prefsEditor.apply();
            } else if ("/GROUPS_REMOVE".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String name = map.getString("name");

                //Remove group from shared preferences
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(this.getApplicationContext());
                final Set<String> stringSet = appSharedPrefs.getStringSet("groups", new HashSet<String>());
                stringSet.remove(name);
                final String contents = appSharedPrefs.getString(name, "");
                SharedPreferences.Editor edit = appSharedPrefs.edit();
                edit.remove(name);
                edit.putStringSet("groups", stringSet);
                edit.commit();
            }
        }
    }
}
