package de.tum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.tum.multiplayer.MultiplayerActivity;

public class WelcomeActivity extends Activity {

	// Debugging
	private static final String TAG = "WelcomeActivity";
	private static final boolean D = true;

	private Button singlePlayerButton;
	private Button multiPlayerButton;
	private Button settingsButton;
	private Button aboutButton;
	private Button exitButton;
	private Button helpButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		// Initialize the buttons
		singlePlayerButton = (Button) findViewById(R.id.singleplayer);
		multiPlayerButton = (Button) findViewById(R.id.multiplayer);
		settingsButton = (Button) findViewById(R.id.settings);
		aboutButton = (Button) findViewById(R.id.about);
		exitButton = (Button) findViewById(R.id.exit);
		helpButton = (Button) findViewById(R.id.help);

		singlePlayerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent singlePlayerGame = new Intent(getApplicationContext(),
						MenschAergereDichNichtActivity.class);
				startActivity(singlePlayerGame);

			}
		});

		multiPlayerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent multiplayerGame = new Intent(getApplicationContext(),
						MultiplayerActivity.class);
				startActivity(multiplayerGame);
			}
		});

		settingsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent gameSettings = new Intent(getApplicationContext(),
						SettingsActivity.class);
				startActivity(gameSettings);
			}
		});

		aboutButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent aboutAct = new Intent(getApplicationContext(),
						AboutActivity.class);
				startActivity(aboutAct);
			}
		});

		exitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});
		
		helpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent helpAct = new Intent(getApplicationContext(),
						HelpActivity.class);
				startActivity(helpAct);
			}
		});		
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "- ON START -");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "- ON RESUME -");
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
}
