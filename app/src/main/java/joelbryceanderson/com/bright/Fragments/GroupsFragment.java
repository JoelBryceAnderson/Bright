package joelbryceanderson.com.bright.Fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import joelbryceanderson.com.bright.Activities.GroupPickerActivity;
import joelbryceanderson.com.bright.Activities.MainActivity;
import joelbryceanderson.com.bright.Adapters.RecyclerViewAdapterGroups;
import joelbryceanderson.com.bright.Adapters.RecyclerViewAdapterLights;
import joelbryceanderson.com.bright.LightGroup;
import joelbryceanderson.com.bright.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends android.support.v4.app.Fragment {

    private RecyclerView recyclerView;
    private RecyclerViewAdapterGroups adapter;
    private List<LightGroup> lightGroupList;
    private FrameLayout frameLayout;
    private TextView noItemsText;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = (RecyclerView) getView().findViewById(R.id.groups_recycler);
        frameLayout = (FrameLayout) getView().findViewById(R.id.groups_frame_layout);
        noItemsText = (TextView) getView().findViewById(R.id.no_groups_text);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();
        Set<String> stringSet = appSharedPrefs.getStringSet("groups", new HashSet<String>());
        if (!stringSet.isEmpty()) {
            noItemsText.setVisibility(View.GONE);
            lightGroupList = new ArrayList<>();
            for (String groupName : stringSet) {
                String json = appSharedPrefs.getString(groupName, "");
                LightGroup group = gson.fromJson(json, LightGroup.class);
                lightGroupList.add(group);
            }
            MainActivity parent = (MainActivity) getActivity();
            adapter = new RecyclerViewAdapterGroups(lightGroupList, parent.getBridge(), this);
            recyclerView.setAdapter(adapter);
        }
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
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();
        Set<String> stringSet = appSharedPrefs.getStringSet("groups", new HashSet<String>());
        if (!stringSet.isEmpty()) {
            lightGroupList = new ArrayList<>();
            for (String groupName : stringSet) {
                String json = appSharedPrefs.getString(groupName, "");
                LightGroup group = gson.fromJson(json, LightGroup.class);
                lightGroupList.add(group);
            }
            MainActivity parent = (MainActivity) getActivity();
            adapter = new RecyclerViewAdapterGroups(lightGroupList, parent.getBridge(), this);
            recyclerView.setAdapter(adapter);
            noItemsText.setVisibility(View.GONE);
        }
    }

    public void addNewGroup() {
        Intent intent = new Intent(getContext(), GroupPickerActivity.class);
        startActivity(intent);
    }

    public void removeGroup(final String name, final int position) {
        final LightGroup group = lightGroupList.remove(position);
        adapter.notifyItemRemoved(position);
        final SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        final Set<String> stringSet = appSharedPrefs.getStringSet("groups", new HashSet<String>());
        stringSet.remove(name);
        final String contents = appSharedPrefs.getString(name, "");
        SharedPreferences.Editor edit = appSharedPrefs.edit();
        edit.remove(name);
        edit.putStringSet("groups", stringSet);
        edit.commit();
        Snackbar snackbar = Snackbar.make(frameLayout, "Group deleted",
                Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar1 = Snackbar.make(
                        frameLayout, "Group restored", Snackbar.LENGTH_SHORT);
                snackbar1.show();
                lightGroupList.add(position, group);
                stringSet.add(name);
                SharedPreferences.Editor edit = appSharedPrefs.edit();
                edit.putStringSet("groups", stringSet);
                edit.putString(name, contents);
                adapter.notifyItemInserted(position);
                edit.commit();
            }
        });
        snackbar.show();
    }
}
