package de.tum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import de.tum.models.ClassicBoard;
import de.tum.models.Dice;
import de.tum.multiplayer.MultiplayerActivity;
import de.tum.multiplayer.TeamMatching;

public class WelcomeActivity extends Activity {
	// result code
	private static final int code_result_single_player = 123;
	// Debugging
	private static final String TAG = "WelcomeActivity";
	private static final boolean D = false;
	
	private static boolean loaded;

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
				new AlertDialog.Builder(WelcomeActivity.this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.button_exit)
		        .setMessage(R.string.confirm_exit)
		        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {

		                //Stop the activity
		                WelcomeActivity.this.finish();    
		            }

		        })
		        .setNegativeButton(R.string.no, null)
		        .show();

			}
		});

		findViewById(R.id.help).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						HelpActivity.class));
			}
		});
		
		// loading settings
		SettingsActivity.loadConfiguration(PreferenceManager.getDefaultSharedPreferences(this));
		if (!loaded) {
		      Room.addRenderable(new ClassicBoard(true));
		      Room.addRenderable(new Dice(true));
		      loaded = true;
		}
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // user has to confirm that he wants to exit
				new AlertDialog.Builder(WelcomeActivity.this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.button_exit)
		        .setMessage(R.string.confirm_exit)
		        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {

		                //Stop the activity
		                WelcomeActivity.this.finish();    
		            }
		        })
		        .setNegativeButton(R.string.no, null)
		        .show();
		}
		else
			return false;
		return true;
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
