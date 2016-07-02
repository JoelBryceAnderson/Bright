package joelbryceanderson.com.bright.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;

import java.util.List;

import joelbryceanderson.com.bright.Activities.MainActivity;
import joelbryceanderson.com.bright.Adapters.RecyclerViewAdapterLights;
import joelbryceanderson.com.bright.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class LightsFragment extends android.support.v4.app.Fragment {

    RecyclerViewAdapterLights adapter;

    public LightsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lights, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.lights_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        PHHueSDK phHueSDK = PHHueSDK.getInstance();
        if (phHueSDK.getSelectedBridge() == null) {
            ((MainActivity)getActivity()).restartSplashActivity();
        }
        PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
        List<PHLight> myLights = cache.getAllLights();
        MainActivity parent = (MainActivity) getActivity();

        int totalLightsOn = 0;
        for (PHLight thisLight : myLights) {
            if (thisLight.getLastKnownLightState().isOn()) {
                totalLightsOn++;
            }
        }
        if (totalLightsOn > (myLights.size() / 2)) {
            parent.setFabTogglesOff();
        }

        adapter = new RecyclerViewAdapterLights(myLights, parent.getBridge());
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
    }

    public void toggleAll(boolean on) {
        adapter.toggleAll(on);
    }
}
