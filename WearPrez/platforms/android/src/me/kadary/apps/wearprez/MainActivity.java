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

package me.kadary.apps.wearprez;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import org.apache.cordova.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends CordovaActivity implements MessageApi.MessageListener{

    private static final String TAG = "WearPrez MainActivity";
    public static final String PRESENTATION_MESSAGE_PATH = "/presentation";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate Mobile");
        super.onCreate(savedInstanceState);
        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "Enter OnMessageReceive " +  messageEvent.getSourceNodeId());
        if (messageEvent.getPath().equals(PRESENTATION_MESSAGE_PATH)) {
            Log.i(TAG, String.valueOf(messageEvent.getData()));
        }

    }
}
