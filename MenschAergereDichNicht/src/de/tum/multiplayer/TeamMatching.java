package de.tum.multiplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import de.tum.R;

public class TeamMatching extends Activity {
	private TeamMatch[] matches;
	
	private class TeamMatch {
		private boolean enabled;
		private boolean human;
		private int startegy;
		private int device;
		
		private CheckBox box_enabled;
		private CheckBox box_human;
		private Spinner spinner_strategy;
		private Spinner spinner_devices;
		
		public TeamMatch(int id_box_enabled, int id_box_human, int id_spinner_startegy, int id_spinner_devices) {
			box_enabled = (CheckBox) findViewById(id_box_enabled);
			box_enabled.setChecked(enabled = true);
			box_enabled.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					enabled = box_enabled.isChecked();
					box_human.setEnabled(enabled);
					spinner_strategy.setEnabled(enabled && !human);
					spinner_devices.setEnabled(enabled);
				}
			});

			box_human = (CheckBox) findViewById(id_box_human);
			box_human.setEnabled(enabled);
			box_human.setChecked(human = true);
			box_human.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
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
			spinner_strategy.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					startegy = pos;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

			spinner_devices = (Spinner) findViewById(id_spinner_devices);
			spinner_devices.setEnabled(enabled);
			adapter = ArrayAdapter.createFromResource(
					TeamMatching.this, R.array.avaible_devices, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner_devices.setAdapter(adapter);
			spinner_devices.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					device = pos;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.match_players);

		matches = new TeamMatch[4];
		matches[0] = new TeamMatch(R.id.checkBoxTeamEnabled1, R.id.checkBoxTeamHuman1, R.id.spinnerAIStrategy1, R.id.spinnerDevice1);
		matches[1] = new TeamMatch(R.id.checkBoxTeamEnabled2, R.id.checkBoxTeamHuman2, R.id.spinnerAIStrategy2, R.id.spinnerDevice2);
		matches[2] = new TeamMatch(R.id.checkBoxTeamEnabled3, R.id.checkBoxTeamHuman3, R.id.spinnerAIStrategy3, R.id.spinnerDevice3);
		matches[3] = new TeamMatch(R.id.checkBoxTeamEnabled4, R.id.checkBoxTeamHuman4, R.id.spinnerAIStrategy4, R.id.spinnerDevice4);
//		// Set result CANCELED in case of back button is pressed
//		setResult(MultiplayerActivity.RESULT_GOBACK);
	}
}