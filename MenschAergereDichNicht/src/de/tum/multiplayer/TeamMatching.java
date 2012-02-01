package de.tum.multiplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import de.tum.R;

/**
 * a team matching shows a dialog for setting up the players and matching them to the available devices.
 * The minimum of available devices is always one - it's simply the current device.
 * This activity can be used for single player mode as well as multiplayer mode
 */
public class TeamMatching extends Activity {
	/** the keys for extra data for this activity */
	public static final String key = "devices";
	/** this says that a player is disabled */
	public static final int int_disabled = -1;
	/** this says that a player is disabled */
	public static final int int_human = 1;
	/** offset for strategy  */
	public static final int offset_strategy = 100;
	/** one team match for every team */
	private TeamMatch matches[];
	/** available devices */
	private String devices[];
	
	/**
	 * a team match holds the settings for setting up one player and matching it to the available devices.
	 */
	private class TeamMatch {
		/** true if player is enabled */
		private boolean enabled;
		/** true if player is human */
		private boolean human;
		/** the strategy of this AI-player */
		private int strategy;
		/** the device of this player */
		private int device;
		
		// the views of this team match
		private CheckBox box_enabled;
		private CheckBox box_human;
		private Spinner spinner_strategy;
		private Spinner spinner_devices;
		
		/**
		 * creating a team match
		 * 
		 * @param id_box_enabled
		 *            the id of the box for enabling this player
		 * @param id_box_human
		 *            the id of the box for making this player human
		 * @param id_spinner_startegy
		 *            the id of the spinner to defining the strategy for this player
		 * @param id_spinner_devices
		 *            the id of the spinner for setting the device of this player
		 * @param team
		 *            the id of the team
		 */
		public TeamMatch(int id_box_enabled, int id_box_human, int id_spinner_startegy, int id_spinner_devices, int team) {
			// setting up GUI
			box_enabled = (CheckBox) findViewById(id_box_enabled);
			box_enabled.setChecked(enabled = true);
			box_enabled.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// setting enabled and updating some views
					enabled = box_enabled.isChecked();
					box_human.setEnabled(enabled);
					spinner_strategy.setEnabled(enabled && !human);
					spinner_devices.setEnabled(devices.length != 1 && enabled);
				}
			});

			box_human = (CheckBox) findViewById(id_box_human);
			box_human.setEnabled(enabled);
			box_human.setChecked(human = devices.length != 1 ? true : team == 0);
			box_human.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// setting human and updating some views
					human = box_human.isChecked();
					spinner_strategy.setEnabled(!human);
				}
			});

			spinner_strategy = (Spinner) findViewById(id_spinner_startegy);
			spinner_strategy.setEnabled(!human);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
					TeamMatching.this, R.array.strategy, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner_strategy.setAdapter(adapter);
			if (devices.length == 1 && team != 0)
				spinner_strategy.setSelection(team - 1);
			spinner_strategy.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					// setting the strategy
					strategy = pos;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

			spinner_devices = (Spinner) findViewById(id_spinner_devices);
			adapter = new ArrayAdapter<CharSequence>(TeamMatching.this,
					android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// adding devices
			for (String device : devices)
				adapter.add(device);
			spinner_devices.setAdapter(adapter);
			if (devices.length == 1)
				spinner_devices.setEnabled(false);
			else {
				spinner_devices.setEnabled(enabled);
				spinner_devices.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view,
							int pos, long id) {
						// setting the device
						device = pos;
					}
	
					@Override
					public void onNothingSelected(AdapterView<?> parent) {}
				});
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String devices[] = getIntent().getExtras().getStringArray(key);
		// adding all devices together
		int amount = devices != null ? devices.length : 0;
		this.devices = new String[amount + 1];
		this.devices[0] = getString(R.string.my_device);
		while (amount > 0)
			this.devices[amount] = devices[amount-- - 1];

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.match_players);

		// setting up team matches
		matches = new TeamMatch[4];
		matches[0] = new TeamMatch(R.id.checkBoxTeamEnabled1, R.id.checkBoxTeamHuman1, R.id.spinnerAIStrategy1, R.id.spinnerDevice1, 0);
		matches[1] = new TeamMatch(R.id.checkBoxTeamEnabled2, R.id.checkBoxTeamHuman2, R.id.spinnerAIStrategy2, R.id.spinnerDevice2, 1);
		matches[2] = new TeamMatch(R.id.checkBoxTeamEnabled3, R.id.checkBoxTeamHuman3, R.id.spinnerAIStrategy3, R.id.spinnerDevice3, 2);
		matches[3] = new TeamMatch(R.id.checkBoxTeamEnabled4, R.id.checkBoxTeamHuman4, R.id.spinnerAIStrategy4, R.id.spinnerDevice4, 3);

		// adding listener for confirm button
		((Button) findViewById(R.id.button_confirm_match)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// checking if at least one player is enabled
				for (int i = 0; i < matches.length; ++i)
					if (i == 5) { // no enabled players found
						MultiplayerActivity.showToast(R.string.no_players);
						return;
					}
					else if (matches[i].enabled) // found one enabled player
						break;
				// creating the players configuration
				int[] players = new int[TeamMatching.this.devices.length * 4];
				// setting the cofiguration
				for (int i = 0; i < matches.length; ++i) {
					TeamMatch match = matches[i];
					if (!match.enabled) { // player disabled
						for (int device = 0; device < players.length / 4; ++device)
							players[device * 4 + i] = int_disabled;
						continue;
					}
					if (match.human) // human player
						players[match.device * 4 + i] = int_human;
					else // AI-player
						players[match.device * 4 + i] = match.strategy + offset_strategy;
				}
				// set result
				Intent intent = new Intent();
				intent.putExtra(key, players);
				TeamMatching.this.setResult(RESULT_OK, intent);
				TeamMatching.this.finish();
			}
		});
	}
}