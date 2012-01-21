package de.tum.multiplayer;

import de.tum.R;
import de.tum.WelcomeActivity;
import de.tum.bluetooth.BluetoothMPService;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ModeSelectionActivity extends Activity {
	// Debugging
	private static final String TAG = "ModeSelectionActivity";
	private static final boolean D = true;

	// Message types sent from the BluetoothMPService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_TITLE = 6;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String TITLE = "title";
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	private String device1;

	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.mode_selection);

		// Set result CANCELED incase the user backs out TODO doesn't work
		setResult(Activity.RESULT_CANCELED);

		Button serverModeButton = (Button) findViewById(R.id.button_servermode);
		serverModeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setupServerMode();
				v.setVisibility(View.GONE);
			}
		});

		Button clientModeButton = (Button) findViewById(R.id.button_clientmode);
		clientModeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				finish();
			}
		});
		
		Button discoveryButton = (Button) findViewById(R.id.button_discovery);
		discoveryButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDiscovery();
			}
		});		

		ListView connectedDevicesList = (ListView) findViewById(R.id.connected_devices);

		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		connectedDevicesList.setAdapter(mNewDevicesArrayAdapter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void startDiscovery() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void setupServerMode() {
		if (D)
			Log.d(TAG, "setupServerMode()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.waiting_others);

		findViewById(R.id.title_connected_devices).setVisibility(View.VISIBLE);
		findViewById(R.id.button_clientmode).setVisibility(View.GONE);

		
		setResult(RESULT_OK); // TODO
	}

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// case MESSAGE_STATE_CHANGE:
			// if (D)
			// Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
			// switch (msg.arg1) {
			// case BluetoothMPService.STATE_ALL_CONNECTED:
			// // mTitle.setText(R.string.title_connected_to);
			// // mTitle.append(mConnectedDeviceName);
			// // mConversationArrayAdapter.clear();
			// Toast.makeText(getApplicationContext(), "Multiplayer Mode",
			// Toast.LENGTH_SHORT).show();
			// break;
			// case BluetoothMPService.STATE_CONNECTED:
			// // mTitle.setText(R.string.title_connected_to);
			// // mTitle.append(mChatService.connectedDevices +
			// // " Device(s)");
			// Toast.makeText(
			// getApplicationContext(),
			// mBluetoothMPService.connectedDevices
			// + " Device(s) connected",
			// Toast.LENGTH_SHORT).show();
			// break;
			// case BluetoothMPService.STATE_CONNECTED_TO_SERVER:
			// // mTitle.setText(R.string.title_connected_to);
			// // mTitle.append(mChatService.connectedDevices +
			// // " Device(s)");
			// Toast.makeText(getApplicationContext(),
			// "Connected to Server" + mConnectedServerName,
			// Toast.LENGTH_SHORT).show();
			// break;
			// case BluetoothMPService.STATE_CONNECTING:
			// // mTitle.setText(R.string.title_connecting);
			// Toast.makeText(getApplicationContext(), "Connecting...",
			// Toast.LENGTH_SHORT).show();
			// break;
			// case BluetoothMPService.STATE_LISTEN:
			// break;
			// case BluetoothMPService.STATE_NONE:
			// // mTitle.setText(R.string.title_not_connected);
			// Toast.makeText(getApplicationContext(), "Not Conntected",
			// Toast.LENGTH_SHORT).show();
			// break;
			// }
			// break;
			// case MESSAGE_WRITE:
			// byte[] writeBuf = (byte[]) msg.obj;
			// // construct a string from the buffer
			// // String writeMessage = new String(writeBuf);
			// // mConversationArrayAdapter.add("Me:  " + writeMessage);
			// break;
			// case MESSAGE_READ:
			// byte[] readBuf = (byte[]) msg.obj;
			// // construct a string from the valid bytes in the buffer
			// // String readMessage = new String(readBuf, 0, msg.arg1);
			// // int deviceID = msg.arg2;
			// // mConversationArrayAdapter.add("Device "+ deviceID + ": " +
			// // readMessage);
			// break;
			case MESSAGE_DEVICE_NAME:
				device1 = msg.getData().getString(DEVICE_NAME);
				mNewDevicesArrayAdapter.add(device1);

				break;
			// case MESSAGE_TOAST:
			// Toast.makeText(getApplicationContext(),
			// msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
			// .show();
			// break;
			// case MESSAGE_TITLE:
			// titleBar.setText(msg.getData().getString(TITLE));
			// break;
			}
		}
	};

}
