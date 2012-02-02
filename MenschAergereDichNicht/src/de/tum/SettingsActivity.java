package de.tum;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.tum.models.ClassicPeg;
import de.tum.models.Dice;
import de.tum.models.Peg;
import de.tum.renderable.GameObject;

/**
 * the settings screen allows to edit lots of settings, like enabling animations
 * or setting the speed
 */
public class SettingsActivity extends Activity{
	/** the screen components */
	private static CheckBox shaking;
	private static CheckBox zooming;
	private static CheckBox animated_peg;
	private static CheckBox animated_dice;
	private static CheckBox rotating_board;
	private static CheckBox classic_peg;
	private static SeekBar rotation_speed;
	private static SeekBar speed;
	
	/** screen is created */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		// adding listeners
		shaking = (CheckBox) findViewById(R.id.checkBoxOptionsShaking);
		shaking.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GameListener.shaking = shaking.isChecked();
			}
		});

		zooming = (CheckBox) findViewById(R.id.checkBoxOptionsZoom);
		zooming.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GameRenderer.zooming = zooming.isChecked();
			}
		});

		animated_peg = (CheckBox) findViewById(R.id.checkBoxOptionsPeg);
		animated_peg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Peg.moving = animated_peg.isChecked();
			}
		});

		animated_dice = (CheckBox) findViewById(R.id.checkBoxOptionsDice);
		animated_dice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Dice.moving = animated_dice.isChecked();
			}
		});

		rotating_board = (CheckBox) findViewById(R.id.checkBoxOptionsRotatingBoard);
		rotating_board.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GameRenderer.rotating = rotating_board.isChecked();
				rotation_speed.setEnabled(GameRenderer.rotating);
			}
		});

		classic_peg = (CheckBox) findViewById(R.id.checkBoxOptionsPegsClassic);
		classic_peg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClassicPeg.used = classic_peg.isChecked();
			}
		});

		rotation_speed = (SeekBar) findViewById(R.id.seekBarSpeedRotation);
		rotation_speed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				GameRenderer.rotation_speed = Math.max(1, seekBar.getProgress()) / 10.0f;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				return;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				return;
			}
		});

		speed = (SeekBar) findViewById(R.id.seekBarSpeed);
		speed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				GameObject.frames = Math.max(1, seekBar.getMax() - seekBar.getProgress());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				return;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				return;
			}
		});

		// button for resetting
		((Button) findViewById(R.id.button_settings_reset))
				.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences settings = PreferenceManager.
						getDefaultSharedPreferences(SettingsActivity.this);
				settings.edit().clear().commit();
				loadConfiguration(settings);
				setValues();
			}
		});
		
		setValues();
	}

	/** {@inheritDoc} */
	@Override
	public final void onStop() {
		super.onStop();
		saveConfiguration(PreferenceManager.getDefaultSharedPreferences(this));
	}
	
	/** setting all values (updating GUI) */
	private static final void setValues() {
		shaking.setChecked(GameListener.shaking);
		zooming.setChecked(GameRenderer.zooming);
		animated_peg.setChecked(Peg.moving);
		animated_dice.setChecked(Dice.moving);
		rotating_board.setChecked(GameRenderer.rotating);
		classic_peg.setChecked(ClassicPeg.used);
		rotation_speed.setEnabled(GameRenderer.rotating);
		rotation_speed.setProgress((int) (GameRenderer.rotation_speed * 10));
		speed.setProgress(speed.getMax() - GameObject.frames);
	}

	/** loading all values from preference file */
	public static final void loadConfiguration(SharedPreferences settings) {
		// reading the stored settings
		GameListener.shaking = settings.getBoolean("enable_shaking", true);
		GameRenderer.zooming = settings.getBoolean("enable_zoom", true);
		Peg.moving = settings.getBoolean("animated_peg", true);
		Dice.moving = settings.getBoolean("animated_dice", true);
		GameRenderer.rotating = settings.getBoolean("rotating_board", true);
		ClassicPeg.used = settings.getBoolean("classic_peg", true);
		GameRenderer.rotation_speed = settings.getFloat("rotation_speed", 1.0f);
		GameObject.frames = settings.getInt("speed", 20);
	}

	/** saving all values to preference file */
	public static final void saveConfiguration(SharedPreferences settings) {
		// writing the stored settings
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("enable_shaking", GameListener.shaking);
		editor.putBoolean("enable_zoom", GameRenderer.zooming);
		editor.putBoolean("animated_peg", Peg.moving);
		editor.putBoolean("animated_dice", Dice.moving);
		editor.putBoolean("rotating_board", GameRenderer.rotating);
		editor.putBoolean("classic_peg", ClassicPeg.used);
		editor.putFloat("rotation_speed", GameRenderer.rotation_speed);
		editor.putInt("speed", GameObject.frames);
		// committing the settings
		editor.commit();
	}
}
