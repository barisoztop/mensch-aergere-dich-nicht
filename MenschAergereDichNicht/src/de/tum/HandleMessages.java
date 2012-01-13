package de.tum;

import de.tum.bluetooth.BluetoothMPService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class HandleMessages extends Handler{
	
    // Debugging
    private static final String TAG = "HandleMessages";
    private static final boolean D = true;	
	
    // Message types sent from the BluetoothMPService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private Context applicationContext;
	
	public HandleMessages(Context applicationContext) {
		this.applicationContext = applicationContext;
		if(D) Log.d(TAG, "HandleMessages created!!!!");
	}

	@Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_STATE_CHANGE:
            if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
            switch (msg.arg1) {
            case BluetoothMPService.STATE_CONNECTED:
//                mTitle.setText(R.string.title_connected_to);
//                mTitle.append(mConnectedDeviceName);
//                mConversationArrayAdapter.clear();
            	Toast.makeText(applicationContext, "Conntected to " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();
                break;
            case BluetoothMPService.STATE_CONNECTING:
//                mTitle.setText(R.string.title_connecting);
            	Toast.makeText(applicationContext, "Connecting...",
                        Toast.LENGTH_SHORT).show();  
                break;
            case BluetoothMPService.STATE_LISTEN:
            case BluetoothMPService.STATE_NONE:
//                mTitle.setText(R.string.title_not_connected);
            	Toast.makeText(applicationContext, "Not Conntected",
                        Toast.LENGTH_SHORT).show();                	
                break;
            }
            break;
        case MESSAGE_WRITE:
            byte[] writeBuf = (byte[]) msg.obj;
            // construct a string from the buffer
//            String writeMessage = new String(writeBuf);
//            mConversationArrayAdapter.add("Me:  " + writeMessage);
            break;
        case MESSAGE_READ:
            byte[] readBuf = (byte[]) msg.obj;
            // construct a string from the valid bytes in the buffer
//            String readMessage = new String(readBuf, 0, msg.arg1);
//            mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
            break;
        case MESSAGE_DEVICE_NAME:
            // save the connected device's name
            mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
            Toast.makeText(applicationContext, "Connected to "
                           + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            break;
        case MESSAGE_TOAST:
            Toast.makeText(applicationContext, msg.getData().getString(TOAST),
                           Toast.LENGTH_SHORT).show();
            break;
        }
    }

}
