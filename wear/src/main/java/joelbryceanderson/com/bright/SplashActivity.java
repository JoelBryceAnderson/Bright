package joelbryceanderson.com.bright;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SplashActivity extends Activity implements
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private Node peerNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(stub1 -> {
            mGoogleApiClient = new GoogleApiClient.Builder(SplashActivity.this)
                    .addConnectionCallbacks(SplashActivity.this)
                    .addOnConnectionFailedListener(result -> {
                        System.out.println(result.getErrorCode());
                        Toast.makeText(getApplicationContext(), "Could not connect",Toast.LENGTH_SHORT).show();
                    })
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.connect();
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

            result.setResultCallback(sendMessageResult -> {
                peerNode = node;

                String connectBridge = "/lights/group/" + "connectBridge";

                PendingResult<MessageApi.SendMessageResult> result1 = Wearable.MessageApi.sendMessage(
                        mGoogleApiClient,
                        peerNode.getId(),
                        connectBridge,
                        null
                );
                result1.setResultCallback(sendMessageResult1 -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                });
            });
        }
    }
}
