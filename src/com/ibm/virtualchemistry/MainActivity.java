/*
  * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.virtualchemistry;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class MainActivity extends Activity implements SensorEventListener{
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView mTitle;
    public static TextView mInfo1;
    public static TextView mInfo2;
    private LinearLayout mLayout;
        
    private MyGLSurfaceView mGLView;
	private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor lightSensor;
    public static boolean bluetoothConnection;
    private JSONObject obj;
    public static JSONValues info;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
      //Load in json file
        obj = loadJSONFromAsset("samplereaction.json");
        try {
			info = new JSONValues(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTitle.setTextColor(0xFFFF0000);
        mInfo1 = (TextView) findViewById(R.id.title_left_text2);
        mInfo2 = (TextView) findViewById(R.id.title_right_text2);

        mLayout = (LinearLayout) findViewById(R.id.main);  
        
        // Get local Bluetooth adapter
        BluetoothClass.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (BluetoothClass.bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
      //Create a GL Surface View instance
      mGLView = new MyGLSurfaceView(this);

      //add GL view on top of main view
      mLayout.addView(mGLView,0);
  
      //initialize various sensors
      mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
      lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!BluetoothClass.bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (BluetoothClass.chatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (BluetoothClass.chatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (BluetoothClass.chatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              BluetoothClass.chatService.start();
            }
        }
        
        mGLView.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        BluetoothClass.chatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        BluetoothClass.outStringBuffer = new StringBuffer("");

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
        mGLView.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (BluetoothClass.chatService != null) BluetoothClass.chatService.stop();
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (BluetoothClass.bluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (BluetoothClass.chatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            BluetoothClass.chatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            BluetoothClass.outStringBuffer.setLength(0);
        }
    }


    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak") 
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        	switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(BluetoothClass.connectedDeviceName);
                    mTitle.setTextColor(0xff00ff00);
                    bluetoothConnection = true;
                    
                    if(BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_PRIMARY) {
            			mInfo1.setText(info.chemical1Name);
            			mInfo2.setText(info.chemical1Formula);
            		}
            		else {
        				mInfo1.setText(info.chemical2Name);
            			mInfo2.setText(info.chemical2Formula);
            		}
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    bluetoothConnection = false;
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    bluetoothConnection = false;
                    break;
            }
            break;
            case MESSAGE_WRITE:
                /*byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);*/
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                BluetoothClass.message = readMessage;
                
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                BluetoothClass.connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + BluetoothClass.connectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = BluetoothClass.bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        BluetoothClass.chatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        case R.id.about:
        	AboutDialog about = new AboutDialog(this);
        	about.setTitle("About");
        	about.show();
        	
	        return true;
        }
        return false;
    }

    float[] mAccel;
    float[] mGeomagnetic;
    float mLightReading;
    long curTime;
    long lastUpdate;
    long diffTime;
    float newX = 0f, newY = 0f, newZ= 0f;
    int shakeThreshold = 1000;
    public static boolean shake = false;
    public static boolean lightReaction = false;

    public void onSensorChanged(SensorEvent event) {
    	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mAccel = event.values;
			float speed = Math.abs(mAccel[0] + mAccel[1] + mAccel[2] - newX - newY - newZ) / 100 * 10000;
			Log.i("#deepti", Float.toString(speed));
	    	if (speed > shakeThreshold && MyGLRenderer.reaction)
	    		shake = true;
	    	newX = mAccel[0];
	    	newY = mAccel[1];
	    	newZ = mAccel[2];
		}		
    	if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagnetic = event.values;
		}
    	if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
    		mLightReading = event.values[0];
    		if (mLightReading > 600 && MyGLRenderer.reaction) {
    			lightReaction = true;
        	}
    	}

    	/*if(shake || lightReaction) {
    		mGLView.reaction();
    	}*/	
    	
    	if (mAccel != null && mGeomagnetic != null) {
    		float R[] = new float[9];
    		float I[] = new float[9];
    		boolean success = SensorManager.getRotationMatrix(R, I, mAccel, mGeomagnetic);
    		if (success) {
    			mGLView.rotate(R);
    		}
    	}
    	if(BluetoothClass.chatService != null && !BluetoothClass.message.contains("null")) {
    		if(BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_PRIMARY) {
    			sendMessage(BluetoothClass.message);
    			BluetoothClass.message = "null";
    		}
    	}
    	if(BluetoothClass.chatService.getDevice() == BluetoothChatService.DEVICE_SECONDARY) {
    		if(MyGLRenderer.colorChangeEffect) {
    			mInfo1.setText(info.productName);
    			mInfo2.setText(info.productFormula);
    		}
    	}	
	}
  
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	/*
	 * fileName-name of file in assets folder
	 * Method returns a json Object from a json resource in the assets folder
	 */
	public JSONObject loadJSONFromAsset(String fileName) {
	    String json = null;
	    try {
	        InputStream in = getAssets().open(fileName);
	        int size = in.available();
	        byte[] buffer = new byte[size];
	        in.read(buffer);
	        in.close();
	        json = new String(buffer, "UTF-8");

	    } catch (IOException ex) {
	        ex.printStackTrace();
	        return null;
	    }
	    
	    try {
			JSONObject jsonObject = new JSONObject (json);
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	    
	}

	
}
