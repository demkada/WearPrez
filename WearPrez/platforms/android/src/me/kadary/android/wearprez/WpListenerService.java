package me.kadary.android.wearprez;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kad on 18/07/15.
 */
public class WpListenerService extends WearableListenerService {

    private static final String TAG = "WearPrez Service";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    public static final String SWITCH_BETWEEN_SLIDES = "/switch_slide";
    private float xOffset, yOffset, zOffset;
    private static final float threshold = 0.381f; //the distance in meter to do with the arm in 1s


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "Event Received from: " + messageEvent.getSourceNodeId());
        if (messageEvent.getPath().equals(SWITCH_BETWEEN_SLIDES)) {
            Map<Character, Float> commandMap = new HashMap<Character, Float>();
            commandMap = (Map<Character, Float>) convertFromByteArray(messageEvent.getData());
            if (!commandMap.isEmpty()) {
                Log.i(TAG, "Message data: " + commandMap.toString());
                float x = commandMap.get('x');
                float y = commandMap.get('y');
                float z = commandMap.get('z');

                if (x >= threshold || x <= -threshold) {

                }
                if (y >= threshold || y <=-threshold) {

                }
                if (z >= threshold || z <= -threshold) {

                }
            }
        }
        else if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            if (!MainActivity.isActivityVisible()) {

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
