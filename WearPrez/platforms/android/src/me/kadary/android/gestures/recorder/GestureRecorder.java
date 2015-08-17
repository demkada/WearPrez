package me.kadary.android.gestures.recorder;

import android.content.Context;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

import me.kadary.android.wearprez.PrezActivity;

/**
 * Created by kad on 18/07/15.
 */
public class GestureRecorder extends WearableListenerService {

    private static final String TAG = "WearPrez Service";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    public static final String SWITCH_BETWEEN_SLIDES = "/switch_slide";

    final int MIN_GESTURE_SIZE = 8;
    float THRESHOLD = 2;
    boolean isRecording;
    int stepsSinceNoMovement;
    ArrayList<float[]> gestureValues;
    Context context;
    GestureRecorderListener listener;
    boolean isRunning;

    public enum RecordMode {
        MOTION_DETECTION, PUSH_TO_GESTURE
    };

    RecordMode recordMode = RecordMode.MOTION_DETECTION;

    public GestureRecorder(Context context) {
        this.context = context;
    }

    public GestureRecorder() {

    }

    private float calcVectorNorm(float[] values) {
        float norm = (float) Math.sqrt(values[0] * values[0] + values[1] * values[1] +
                values[2] * values[2]) - 9.9f;
        return norm;
    }


    public RecordMode getRecordMode() {
        return recordMode;
    }

    public void setThreshold(float threshold) {
        THRESHOLD = threshold;
        System.err.println("New Threshold " + threshold);
    }

    public boolean isRunning() {
        return isRunning;
    }


    public void onPushToGesture(boolean pushed) {

        if (recordMode == RecordMode.PUSH_TO_GESTURE) {
            isRecording = pushed;
            if (isRecording) {
                gestureValues = new ArrayList<float[]>();
            } else {
                if (gestureValues.size() > MIN_GESTURE_SIZE) {
                    listener.onGestureRecorded(gestureValues);
                }
                gestureValues = null;
            }
        }
    }


    public void registerListener(GestureRecorderListener listener) {
        this.listener = listener;
        start();
    }

    public void setRecordMode(RecordMode recordMode) {
        this.recordMode = recordMode;
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public void unregisterListener(GestureRecorderListener listener) {
        this.listener = null;
        stop();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "Message Received from: " + messageEvent.getSourceNodeId());
        if (messageEvent.getPath().equals(SWITCH_BETWEEN_SLIDES)) {
            Map<Character, Float> commandMap = (Map<Character, Float>)
                    convertFromByteArray(messageEvent.getData());
            float[] value = { commandMap.get('x'), commandMap.get('y'), commandMap.get('z') };
            switch (recordMode) {
                case MOTION_DETECTION:
                    if (isRecording) {
                        gestureValues.add(value);
                        if (calcVectorNorm(value) < THRESHOLD) {
                            stepsSinceNoMovement++;
                        } else {
                            stepsSinceNoMovement = 0;
                        }
                    } else if (calcVectorNorm(value) >= THRESHOLD) {
                        isRecording = true;
                        stepsSinceNoMovement = 0;
                        gestureValues = new ArrayList<float[]>();
                        gestureValues.add(value);
                    }
                    if (stepsSinceNoMovement == 10) {

                        System.out.println("Length is: " + String.valueOf(gestureValues.size() - 10));
                        if (gestureValues.size() - 10 > MIN_GESTURE_SIZE) {
                            listener.onGestureRecorded(gestureValues.subList(0, gestureValues.size() - 10));
                        }
                        gestureValues = null;
                        stepsSinceNoMovement = 0;
                        isRecording = false;
                    }
                    break;
                case PUSH_TO_GESTURE:
                    if (isRecording) {
                        gestureValues.add(value);
                    }
                    break;
            }
        }
        else if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            if (!PrezActivity.isActivityVisible()) {

            }
        }
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
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "convertFromByteArray: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "convertFromByteArray: " + e.getMessage());
        }
        return object;
    }
}
