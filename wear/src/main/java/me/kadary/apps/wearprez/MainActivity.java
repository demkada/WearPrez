package me.kadary.apps.wearprez;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.kadary.android.wearprez.R;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    private String presentationNodeId = null;
    private static final String PRESENTATION_CAPABILITY_NAME = "presentation";
    public static final String PRESENTATION_MESSAGE_PATH = "/presentation";
    private GoogleApiClient mGoogleApiClient;

    private static final String TAG = "WearPrez MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        new Thread( new Runnable() {
            @Override
            public void run() {
                setupPresentation();
            }
        }).start();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        Map<Character, Float> commandMap = new HashMap<>();
        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            //if (x >= 1 || x <= -1) {
                commandMap.put('x',x);
                Log.i(TAG, "X=" + x);
            //}
            //if (y >= 1 || y <=-1) {
                commandMap.put('y',y);
                Log.i(TAG, "Y=" + y);
            //}
            //if (z >= 1 || z <= -1) {
                commandMap.put('z', z);
                Log.i(TAG, "Z=" + z);
            //}
            //if (!commandMap.isEmpty()) {
                sendCommands(commandMap.toString().getBytes());
            //}
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sendCommands(byte[] commandSet) {
        if (presentationNodeId != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, presentationNodeId,
                    PRESENTATION_MESSAGE_PATH, commandSet).setResultCallback(
                    new ResultCallback() {
                        @Override
                        public void onResult(Result sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message");
                            }
                            else
                                Log.i(TAG, "success!! sent to: " + presentationNodeId);
                        }
                    }
            );
        } else {
            Log.e(TAG, "Unable to retrieve node with presentation capability");
        }
    }



    private void setupPresentation() {
        Log.d(TAG, "SetupPresentation Method");
        CapabilityApi.GetCapabilityResult result =
                Wearable.CapabilityApi.getCapability(
                        mGoogleApiClient, PRESENTATION_CAPABILITY_NAME,
                        CapabilityApi.FILTER_REACHABLE).await();
        Log.d(TAG, "Capabilities: " + result.toString());
        updatePresentationCapability(result.getCapability());

    }

    private void updatePresentationCapability(CapabilityInfo capability) {
        Log.d(TAG, "updatePresentationCapability Method");
        Set<Node> connectedNodes = capability.getNodes();
        presentationNodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> connectedNodes) {
        Log.d(TAG, "pickBestNodeId Method");
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : connectedNodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }
}
