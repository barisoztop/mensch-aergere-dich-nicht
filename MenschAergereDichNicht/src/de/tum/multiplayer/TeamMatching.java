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
	private boolean enabled;
	private boolean human;
	private int startegy;
	private int device;
	
	private CheckBox box_enabled;
	private CheckBox box_human;
	private Spinner spinner_strategy;
	private Spinner spinner_devices;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.match_players);
		
		box_enabled = (CheckBox) findViewById(R.id.checkBoxTeamEnabled);
		box_enabled.setChecked(enabled = true);
		box_enabled.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				enabled = box_enabled.isChecked();
				box_human.setEnabled(enabled);
				spinner_strategy.setEnabled(enabled && !human);
				spinner_devices.setEnabled(enabled);
			}
		});

		box_human = (CheckBox) findViewById(R.id.checkBoxTeamHuman);
		box_human.setEnabled(enabled);
		box_human.setChecked(human = true);
		box_human.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				human = box_human.isChecked();
				spinner_strategy.setEnabled(!human);
			}
		});

		spinner_strategy = (Spinner) findViewById(R.id.spinnerAIStrategy);
		spinner_strategy.setEnabled(!human);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.strategy, android.R.layout.simple_spinner_item);
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

		spinner_devices = (Spinner) findViewById(R.id.spinnerDevice);
		spinner_devices.setEnabled(enabled);
		adapter = ArrayAdapter.createFromResource(
				this, R.array.avaible_devices, android.R.layout.simple_spinner_item);
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

		// Set result CANCELED in case of back button is pressed
		setResult(MultiplayerActivity.RESULT_GOBACK);
	}
}