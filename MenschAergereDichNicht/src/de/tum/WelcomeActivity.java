package de.tum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import de.tum.multiplayer.MultiplayerActivity;
import de.tum.multiplayer.TeamMatching;

public class WelcomeActivity extends Activity {
	// result code
	private static final int code_result_single_player = 123;
	// Debugging
	private static final String TAG = "WelcomeActivity";
	private static final boolean D = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		// adding listeners
		findViewById(R.id.singleplayer).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(new Intent(getApplicationContext(),
								TeamMatching.class).putExtra(
								TeamMatching.key, (String[]) null), code_result_single_player);
					}
				});

		findViewById(R.id.multiplayer).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getApplicationContext(),
								MultiplayerActivity.class));
					}
				});

		findViewById(R.id.settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						SettingsActivity.class));
			}
		});

		findViewById(R.id.about).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						AboutActivity.class));
			}
		});

		findViewById(R.id.exit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();

			}
		});

		findViewById(R.id.help).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						HelpActivity.class));
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == code_result_single_player && resultCode == RESULT_OK)
			startActivity(new Intent(getApplicationContext(),
					MenschAergereDichNichtActivity.class).putExtra(
					TeamMatching.key,
					data.getExtras().getIntArray(TeamMatching.key)));
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
