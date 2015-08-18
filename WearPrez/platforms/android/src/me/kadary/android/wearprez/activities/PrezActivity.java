/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package me.kadary.android.wearprez.activities;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.apache.cordova.CordovaActivity;

import me.kadary.android.gestures.IGestureRecognitionListener;
import me.kadary.android.gestures.IGestureRecognitionService;
import me.kadary.android.gestures.classifier.Distribution;
import me.kadary.android.wearprez.config.GestureCalibrationPreference;

public class PrezActivity extends CordovaActivity {

    private static final String TAG = "WearPrez MainActivity";

    private static boolean activityVisible = false;
    private  static Instrumentation instrumentation = new Instrumentation();

    IGestureRecognitionService recognitionService;
    String activeTrainingSet = GestureCalibrationPreference.activeTrainingSet;

    public static void setActivityVisible(boolean activityVisible) {
        PrezActivity.activityVisible = activityVisible;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate Mobile");
        super.onCreate(savedInstanceState);
        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }

    @Override
    protected void onStart() {
        setActivityVisible(true);
        super.onStart();
    }

    @Override
    protected void onPause() {
        try {
            recognitionService.unregisterListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        recognitionService = null;
        unbindService(serviceConnection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Intent bindIntent = new Intent(this, me.kadary.android.gestures.GestureRecognitionService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    public static boolean isActivityVisible() {

        return activityVisible;
    }

    @Override
    protected void onStop() {
        setActivityVisible(false);
        super.onStop();
    }

    protected static void fireEvent(final int keyEvent) {
        final Thread t = new Thread() {
            public void run() {
                instrumentation.sendKeyDownUpSync(keyEvent);
            }
        };
        t.start();
    }

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
        public void onGestureRecognized(final Distribution distribution) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   /* int keyEvent = KeyEvent.KEYCODE_DPAD_CENTER;
                    boolean negative = Math.signum(value) < 0;
                    switch (axis) {
                        case 'x':
                            if (negative) {
                                keyEvent = KeyEvent.KEYCODE_DPAD_LEFT;
                            }
                            else {
                                keyEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
                            }
                            break;
                        case 'y':
                            if (negative) {
                                keyEvent = KeyEvent.KEYCODE_DPAD_UP;
                            }
                            else {
                                keyEvent = KeyEvent.KEYCODE_DPAD_DOWN;
                            }
                            break;
                        case 'z':

                            break;
                    }
                    MainActivity.fireEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
                    Log.e(TAG, "Value " + value + " has been dispatched to " + axis + " axis!");*/
                }
            });
        }

        @Override
        public void onGestureLearned(String gestureName) throws RemoteException {

        }

        @Override
        public void onTrainingSetDeleted(String trainingSet) throws RemoteException {

        }
    };

}
