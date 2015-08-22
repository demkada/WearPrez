package me.kadary.android.gestures.recorder;

import java.util.EventObject;

/**
 * Created by kad on 22/08/15.
 */
public class WearableEvent extends EventObject {
    private float[] value;

    public WearableEvent(RemoteWearableEvent source, float[] value) {
        super(source);
        this.value = value;
    }

    public float[] getValue() {
        return value;
    }
}
