package de.tum;

import de.tum.models.Peg;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	public static void setConfiguration(SharedPreferences prefs) {
		GameRenderer.zooming = prefs.getBoolean("enable_zoom", true);
		GameRenderer.rotating = prefs.getBoolean("rotating_board", false);
		GameListener.shaking = prefs.getBoolean("enable_shaking", true);
		Peg.moving = prefs.getBoolean("animated_peg", true);
	}
}
