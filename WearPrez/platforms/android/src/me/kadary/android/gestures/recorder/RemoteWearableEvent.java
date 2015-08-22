package me.kadary.android.gestures.recorder;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by kad on 22/08/15.
 */
public class RemoteWearableEvent extends WearableListenerService {
    private static final String TAG = "WearPrez Service";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    public static final String SWITCH_BETWEEN_SLIDES = "/switch_slide";

    private static Set<WearableEventListener> _listeners = new HashSet<WearableEventListener>();
    public static synchronized void addEventListener(WearableEventListener listener)  {
        _listeners.add(listener);
    }
    public static synchronized void removeEventListener(WearableEventListener listener)   {
        _listeners.remove(listener);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(SWITCH_BETWEEN_SLIDES)) {
            Log.i(TAG, "Message Received from: " + messageEvent.getSourceNodeId());
            Map<Character, Float> commandMap = (Map<Character, Float>)
                    convertFromByteArray(messageEvent.getData());
            float[] value = {commandMap.get('x'), commandMap.get('y'), commandMap.get('z')};
            fireEvent(value);
        }
    }

    private synchronized void fireEvent(float[] value) {
        Log.i(TAG, "Firing value: " + "[" + value[0] + "|" + value[1] + "|" + value[2] + "]");
        WearableEvent event = new WearableEvent(this, value);
        Iterator i = _listeners.iterator();
        Log.i(TAG, "Listener list: " + _listeners.toString());
        while (i.hasNext()) {
            ((WearableEventListener) i.next()).onEventReceived(event);
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
