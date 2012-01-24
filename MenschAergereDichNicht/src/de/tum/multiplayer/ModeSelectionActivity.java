package de.tum.multiplayer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import de.tum.R;

public class ModeSelectionActivity extends Activity {
	// Debugging
	private static final String TAG = "ModeSelectionActivity";
	private static final boolean D = true;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.e(TAG, "+++ ON CREATE +++");
		
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.mode_selection);

		// Set result CANCELED in case of back button is pressed
		setResult(Activity.RESULT_CANCELED);

		Button serverModeButton = (Button) findViewById(R.id.button_servermode);
		serverModeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(MultiplayerActivity.RESULT_SERVER_MODE);
				finish();
			}
		});

		Button clientModeButton = (Button) findViewById(R.id.button_clientmode);
		clientModeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(MultiplayerActivity.RESULT_CLIENT_MODE);
				finish();
			}
		});
		
		Button discoveryButton = (Button) findViewById(R.id.button_discovery);
		discoveryButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDiscovery();
			}
		});
	}
	
	private void startDiscovery() {
		if (D) Log.d(TAG, "startDiscovery()");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.e(TAG, "++ ON START ++");
	}
	
	@Override
	public void onResume() {
		super.onResume();
//		if (D) Log.e(TAG, "+ ON RESUME +");
//		// If the adapter is null, then Bluetooth is not supported
//		if (mBluetoothAdapter == null) {
//			Toast.makeText(this, "Bluetooth is not available, try New Game",
//					Toast.LENGTH_LONG).show();
//			finish();
//			return;
//		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (D) Log.e(TAG, "- ON PAUSE -");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (D) Log.e(TAG, "-- ON STOP --");
	}	

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D) Log.e(TAG, "--- ON DESTROY ---");
	}

}
