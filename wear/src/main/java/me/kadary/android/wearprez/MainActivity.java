package me.kadary.android.wearprez;

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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private String presentationNodeId = null;
    private static final String PRESENTATION_CAPABILITY_NAME = "wearPrez";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    public static final String SWITCH_BETWEEN_SLIDES = "/switch_slide";
    private GoogleApiClient mGoogleApiClient;

    private static final String TAG = "WearPrez MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        initAPI();
    }

    private void initAPI() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        Wearable.CapabilityApi.getCapability(
                                mGoogleApiClient, PRESENTATION_CAPABILITY_NAME,
                                CapabilityApi.FILTER_REACHABLE).setResultCallback(
                                new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                                    @Override
                                    public void onResult(CapabilityApi.GetCapabilityResult result) {
                                        setupPresentation(result);
                                    }
                                }
                        );
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
                .build();
    }

    private void setupPresentation(CapabilityApi.GetCapabilityResult result) {
        Log.d(TAG, "SetupPresentation Method");
        Log.d(TAG, "Capability to use: " + result.getCapability().getName());
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

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        Map<Character, Float> commandMap = new HashMap<>();
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            commandMap.put('x', event.values[0]);
            commandMap.put('y', event.values[1]);
            commandMap.put('z', event.values[2]);
            Log.i(TAG, "CommandMap: " + commandMap.toString());

            sendMessage(convertToByteArray(commandMap), SWITCH_BETWEEN_SLIDES);
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
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void sendMessage(final byte[] messageData, String messagePath) {
        if (presentationNodeId != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, presentationNodeId,
                    messagePath, messageData).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult result) {
                            if (!result.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message");
                            }
                            else
                                Log.i(TAG, "success!! Message sent to: " + presentationNodeId);
                        }
                    }
            );
        } else {
            Log.e(TAG, "Unable to retrieve node with presentation capability");
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private byte[] convertToByteArray(Object object) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(data);
            outputStream.writeObject(object);
        } catch (IOException e) {
            Log.e(TAG, "convertToByteArray: " + e.getMessage());
        }
        return data.toByteArray();
    }

    private Object convertFromByteArray(byte[] data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Object object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            object = objectInputStream.readObject();
        }  catch (Exception e) {
            Log.e(TAG, "convertFromByteArray: " + e.getMessage());
        }
        return object;
    }

}
