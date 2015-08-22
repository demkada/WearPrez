package me.kadary.android.wearprez.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import me.kadary.android.gestures.GestureRecognitionService;
import me.kadary.android.gestures.IGestureRecognitionListener;
import me.kadary.android.gestures.IGestureRecognitionService;
import me.kadary.android.gestures.classifier.Distribution;
import me.kadary.android.wearprez.R;

import static com.google.android.gms.internal.zzhl.runOnUiThread;


public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    IGestureRecognitionService recognitionService;
    String activeTrainingSet;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
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

    IBinder gestureListenerStub = new IGestureRecognitionListener.Stub() {

        @Override
        public void onGestureLearned(String gestureName) throws RemoteException {
            Toast.makeText(getActivity(), String.format("Gesture %s learned", gestureName), Toast.LENGTH_SHORT).show();
            Log.e("WearPrez Gesture", "Gesture %s learned");
        }

        @Override
        public void onTrainingSetDeleted(String trainingSet) throws RemoteException {
            Toast.makeText(getActivity(), String.format("Training set %s deleted", trainingSet), Toast.LENGTH_SHORT).show();
            Log.e("WearPrez Gesture", String.format("Training set %s deleted", trainingSet));
        }

        @Override
        public void onGestureRecognized(final Distribution distribution) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), String.format("%s: %f", distribution.getBestMatch(), distribution.getBestDistance()), Toast.LENGTH_LONG).show();
                    Log.e("WearPrez Gesture", String.format("%s: %f", distribution.getBestMatch(), distribution.getBestDistance()));
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        //addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        final TextView activeTrainingSetText = (TextView) rootView.findViewById(R.id.activeTrainingSet);
        final EditText trainingSetText = (EditText) rootView.findViewById(R.id.trainingSetName);
        final EditText editText = (EditText) rootView.findViewById(R.id.gestureName);
        activeTrainingSet = editText.getText().toString();
        final Button startTrainButton = (Button) rootView.findViewById(R.id.trainButton);
        final Button deleteTrainingSetButton = (Button) rootView.findViewById(R.id.deleteTrainingSetButton);
        final Button changeTrainingSetButton = (Button) rootView.findViewById(R.id.startNewSetButton);
        final SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar1);
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setMax(20);
        seekBar.setProgress(20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                try {
                    recognitionService.setThreshold(progress / 10.0f);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        startTrainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (recognitionService != null) {
                    try {
                        Log.e(TAG, "recognitionService: " + recognitionService.isLearning());
                        if (!recognitionService.isLearning()) {
                            startTrainButton.setText("Stop Training");
                            editText.setEnabled(false);
                            deleteTrainingSetButton.setEnabled(false);
                            changeTrainingSetButton.setEnabled(false);
                            trainingSetText.setEnabled(false);
                            recognitionService.startLearnMode(activeTrainingSet, editText.getText().toString());
                        } else {
                            startTrainButton.setText("Start Training");
                            editText.setEnabled(true);
                            deleteTrainingSetButton.setEnabled(true);
                            changeTrainingSetButton.setEnabled(true);
                            trainingSetText.setEnabled(true);
                            recognitionService.stopLearnMode();
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        changeTrainingSetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                activeTrainingSet = trainingSetText.getText().toString();
                activeTrainingSetText.setText(activeTrainingSet);

                if (recognitionService != null) {
                    try {
                        recognitionService.startClassificationMode(activeTrainingSet);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        deleteTrainingSetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("You really want to delete the training set?").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (recognitionService != null) {
                            try {
                                recognitionService.deleteTrainingSet(activeTrainingSet);
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        Intent bindIntent = new Intent(getActivity(), GestureRecognitionService.class);
        getActivity().bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void onPause() {
        try {
            recognitionService.unregisterListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        recognitionService = null;
        getActivity().unbindService(serviceConnection);
        super.onPause();
    }
}
