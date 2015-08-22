package me.kadary.android.gestures.recorder;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kad on 18/07/15.
 */
public class GestureRecorder implements WearableEventListener {
    private static final String TAG = "Gesture Recorder";

    public enum RecordMode {
        MOTION_DETECTION, PUSH_TO_GESTURE
    };

    final int MIN_GESTURE_SIZE = 8;
    float THRESHOLD = 2;
    boolean isRecording;
    int stepsSinceNoMovement;
    ArrayList<float[]> gestureValues;
    Context context;
    GestureRecorderListener listener;
    boolean isRunning;

    RecordMode recordMode = RecordMode.MOTION_DETECTION;

    public GestureRecorder(Context context) {
        this.context = context;
    }

    private float calcVectorNorm(float[] values) {
        float norm = (float) Math.sqrt(values[0] * values[0] + values[1] * values[1] +
                values[2] * values[2]) - 9.9f;
        Log.e(TAG, "VerctorNorm: " + norm);
        return norm;
    }


    public RecordMode getRecordMode() {
        return recordMode;
    }

    public void setThreshold(float threshold) {
        THRESHOLD = threshold;
        Log.e("WearPrez Gesture", "New Threshold " + threshold);
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

    @Override
    public void onEventReceived(WearableEvent wearableEvent) {
        if(isRunning()) {
            float[] value = wearableEvent.getValue();
            Log.e(TAG, "Received value from listener: " + "[" + value[0] + "|" + value[1] + "|" + value[2] + "]");
            Log.e(TAG, "recordMode: " + recordMode);
            Log.e(TAG, "isRecording: " + isRecording);
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
    }

    public void registerListener(GestureRecorderListener listener) {
        this.listener = listener;
        start();
    }

    public void setRecordMode(RecordMode recordMode) {
        this.recordMode = recordMode;
    }

    public void start() {
        RemoteWearableEvent.addEventListener(this);
        isRunning = true;
    }

    public void stop() {
        RemoteWearableEvent.removeEventListener(this);
        isRunning = false;
    }

    public void unregisterListener(GestureRecorderListener listener) {
        this.listener = null;
        stop();
    }

    public void pause(boolean b) {
        if (b) {
            RemoteWearableEvent.removeEventListener(this);
        } else {
            RemoteWearableEvent.addEventListener(this);
        }
    }
}
