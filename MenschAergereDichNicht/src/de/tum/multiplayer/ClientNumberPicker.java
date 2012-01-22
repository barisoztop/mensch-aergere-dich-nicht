package de.tum.multiplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import de.tum.R;

public class ClientNumberPicker extends Activity {
	// Debugging
	private static final String TAG = "ClientNumberPicker";
	private static final boolean D = true;
	private int numberOfClients;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.client_number_picker);
		
//		Intent intent = getIntent();
//		numberOfClients = intent.getIntExtra(MultiplayerActivity.MAX_CLIENTS, 0);

		Button clientNumberButton = (Button) findViewById(R.id.button_clientnumber);
		clientNumberButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "numberOfClients: " + numberOfClients);

				Intent intent = getIntent();
				intent.putExtra(MultiplayerActivity.MAX_CLIENTS, numberOfClients);
				setResult(MultiplayerActivity.RESULT_SERVER_MODE, intent);

				finish();
			}
		});

		Spinner spinnerClientNumber = (Spinner) findViewById(R.id.spinnerClientNumber);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.client_numbers_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerClientNumber.setAdapter(adapter);
		spinnerClientNumber
				.setOnItemSelectedListener(new OnClientNumberSelectedListener());

		// Set result CANCELED in case of back button is pressed
		setResult(MultiplayerActivity.RESULT_GOBACK);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
	}

	@Override
	public void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	public class OnClientNumberSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {

			numberOfClients = pos + 1;
		}

		@Override
		public void onNothingSelected(AdapterView parent) {}
	}

}