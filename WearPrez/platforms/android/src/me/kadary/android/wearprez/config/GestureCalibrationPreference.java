package me.kadary.android.wearprez.config;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import me.kadary.android.gestures.IGestureRecognitionListener;
import me.kadary.android.gestures.IGestureRecognitionService;
import me.kadary.android.gestures.classifier.Distribution;
import me.kadary.android.wearprez.R;

/**
 * Created by kad on 18/08/15.
 */
public class GestureCalibrationPreference extends DialogPreference {

    IGestureRecognitionService recognitionService;
    public static final String activeTrainingSet = "WearPrez_Motion";

    public GestureCalibrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.gesture_calibration_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final IBinder gestureListenerStub = new IGestureRecognitionListener.Stub() {

            @Override
            public void onGestureLearned(String gestureName) throws RemoteException {
                //Toast.makeText(GestureCalibrationPreference.this, String.format("Gesture %s learned", gestureName), Toast.LENGTH_SHORT).show();
                Log.e("WearPrez Gesture", "Gesture %s learned");
            }

            @Override
            public void onTrainingSetDeleted(String trainingSet) throws RemoteException {
                //Toast.makeText(GestureTrainer.this, String.format("Training set %s deleted", trainingSet), Toast.LENGTH_SHORT).show();
                Log.e("WearPrez Gesture", String.format("Training set %s deleted", trainingSet));
            }

            @Override
            public void onGestureRecognized(final Distribution distribution) throws RemoteException {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(GestureTrainer.this, String.format("%s: %f", distribution.getBestMatch(), distribution.getBestDistance()), Toast.LENGTH_LONG).show();
//                    Log.e("WearPrez Gesture", String.format("%s: %f", distribution.getBestMatch(), distribution.getBestDistance()));
//                }
//            });
            }
        };

        final ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                recognitionService = IGestureRecognitionService.Stub.asInterface(service);
                try {
                    recognitionService.startClassificationMode(activeTrainingSet);
                    recognitionService.registerListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                recognitionService = null;
            }
        };

        if (recognitionService != null) {
            try {
                if (!recognitionService.isLearning()) {
                    recognitionService.startLearnMode(activeTrainingSet, "left2right");
                } else {
                    recognitionService.stopLearnMode();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
