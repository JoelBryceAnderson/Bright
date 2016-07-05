package joelbryceanderson.com.bright;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks {

    private RecyclerView wearRecycler;
    private GoogleApiClient mGoogleApiClient;
    private Node peerNode;
    private List<Group> list;
    private RecyclerGroupAdapterWearGroups adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(result -> {
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(stub1 -> {
            //Setup Recycler view
            wearRecycler = (RecyclerView) stub1.findViewById(R.id.wear_groups_recycler);
            wearRecycler.setHasFixedSize(true);
            LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            wearRecycler.setLayoutManager(manager);

            list = new ArrayList<>();
            list.add(new Group("header", false));

            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();
            Set<String> stringSet = appSharedPrefs.getStringSet("groups", new HashSet<>());
            if (!stringSet.isEmpty()) {
                for (String groupName : stringSet) {
                    String json = appSharedPrefs.getString(groupName, "");
                    Group group = gson.fromJson(json, Group.class);
                    list.add(group);
                }

                list.add(new Group("footer", false));
            } else {
                TextView noGroupsText1 = (TextView) stub1.findViewById(R.id.no_groups_text);
                ImageView noGroupsIcon = (ImageView) stub1.findViewById(R.id.no_groups_icon);
                noGroupsText1.setVisibility(View.VISIBLE);
                noGroupsIcon.setVisibility(View.VISIBLE);
                wearRecycler.setVisibility(View.GONE);
            }

            adapter =
                    new RecyclerGroupAdapterWearGroups(list, MainActivity.this);
            wearRecycler.setAdapter(adapter);
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this).setResultCallback(resultCallback);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
    }

    public void turnGroupOff(int position) {
        String togglePath = "/lights/group/" + "off";

        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                peerNode.getId(),
                togglePath,
                list.get(position).getName().getBytes()
        );
    }

    public void turnGroupOn(int position) {
        String togglePath = "/lights/group/" + "on";

        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                peerNode.getId(),
                togglePath,
                list.get(position).getName().getBytes()
        );
    }

    public void changeGroupBrightness(int position, int brightness) {
        String togglePath = "/lights/group/" + "brightness";

        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                peerNode.getId(),
                togglePath,
                (list.get(position).getName() + "`" + Integer.toString(brightness)).getBytes()
        );
    }

    public void changeGroupColor(int position, int color) {
        String togglePath = "/lights/group/" + "color";

        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                peerNode.getId(),
                togglePath,
                (list.get(position).getName() + "`" + Integer.toString(color)).getBytes()
        );
    }

    private ResultCallback<Status> resultCallback = status -> new AsyncTask<Void, Void, Void>(){
        @Override
        protected Void doInBackground(Void... params) {
            sendStartMessage();
            return null;
        }
    }.execute();

    private void sendStartMessage(){

        NodeApi.GetConnectedNodesResult rawNodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (final Node node : rawNodes.getNodes()) {
            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                    mGoogleApiClient,
                    node.getId(),
                    "/start",
                    null
            );

            result.setResultCallback(sendMessageResult -> peerNode = node);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                int brightnessToSet = data.getIntExtra("brightness", 250);
                int positionToSet = data.getIntExtra("position", 1);
                changeGroupBrightness(positionToSet, brightnessToSet);
                adapter.turnOnSwitch(positionToSet);
            }
        } else if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                int colorToSet = data.getIntExtra("color", 0);
                int positionToSet = data.getIntExtra("position", 1);
                changeGroupColor(positionToSet, colorToSet);
                adapter.turnOnSwitch(positionToSet);
            }
        }
    }
}
