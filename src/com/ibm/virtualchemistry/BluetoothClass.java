package com.ibm.virtualchemistry;

import android.bluetooth.BluetoothAdapter;

public class BluetoothClass {
	
	public static String message = "null";
	// Name of the connected device
    public static String connectedDeviceName = null;
    // String buffer for outgoing messages
    public static StringBuffer outStringBuffer;
    // Local Bluetooth adapter
    public static BluetoothAdapter bluetoothAdapter = null;
    // Member object for the chat services
    public static BluetoothChatService chatService = null;
}
