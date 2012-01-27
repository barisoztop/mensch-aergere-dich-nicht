package de.tum;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import de.tum.player.HumanPlayer;

public class GameListener implements OnTouchListener, OnLongClickListener, SensorEventListener {

	// time out
	private static long timeOutForDice = 1000 * 3;
	private static long timeOutForSelection = 1000 * 6;
	private static long timeOutAdditional = 1000 * 4;

	// Minimum distance between two fingers
	private final static float MIN_DIST = 40;
	// Distance between two fingers
	private static float eventDistance = 0;
	// possible touch states
	private final static int NONE = 0;
	private final static int DRAG = 1;
	private final static int ZOOM = 2;
	// current touch state
	private int touchState = NONE;
	
	private static SensorManager manager;
	private static Sensor sensor_accelerometer;
	private static GameListener content;

	private static boolean waitingForInput;
	public static final int waitingForDice = 0;
	public static final int waitingForPegSelection = 1;
	public static final int waitingForPegChosen = 2;
	public static final int waitingTimeOut = 3;
	private static int waitingFor;
	private static HumanPlayer player;
	private static boolean long_press;
	private static long time;
	private static int count;

	private float down_x;
	private float down_y;
	
	private static float acceleration_x;
	private static float acceleration_y;
	private static float acceleration_z;

	private static final Object sync = new Object();
	
	public GameListener(Activity main) {
	    manager = (SensorManager) main.getSystemService(Activity.SENSOR_SERVICE);
	    sensor_accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    content = this;
	}

	public static final void waitForInput(HumanPlayer player, int waitingFor) {
		synchronized (sync) {
			waitingForInput = true;
			GameListener.waitingFor = waitingFor;
			GameListener.player = player;
			count = 0;
			time = System.currentTimeMillis();
		}
	}

	public static final void stopWaiting() {
		synchronized (sync) {
			waitingForInput = false;
		}
	}

	public static final void verifyWaiting() {
		synchronized (sync) {
			if (!waitingForInput)
				return;
			if ((count += count < 0 ? -1 : 1) % 30 == 0
					&& System.currentTimeMillis() - time > (waitingFor == waitingForDice ? timeOutForDice : timeOutForSelection))
				if (count > 0) {
					time += timeOutAdditional;
					count *= -1;
					player.waitedForInput(waitingTimeOut, null);
				} else
					player.waitedForInput(waitingFor == waitingForDice ? waitingForDice
							: waitingForPegChosen, null);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			// primary touch event starts
			down_x = event.getX();
			down_y = event.getY();
			touchState = DRAG;
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			// secondary touch event starts: remember distance and center
			eventDistance = calcDistance(event);
			if (eventDistance > MIN_DIST)
				touchState = ZOOM;
			break;

		case MotionEvent.ACTION_MOVE:
			if (touchState == DRAG) {
				GameRenderer.tranfer(event.getX() - down_x, event.getY()
						- down_y);
				down_x = event.getX();
				down_y = event.getY();

			} else if (touchState == ZOOM) {
//				Log.d(TAG, "ZOOM");
				float dist = calcDistance(event);
				GameRenderer.zoom(1 + ((eventDistance / dist) - 1));
				eventDistance = dist;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (long_press)
				return long_press = !long_press;
			synchronized (sync) {
				if (waitingForInput)
					player.waitedForInput(waitingFor, null);
			}
		case MotionEvent.ACTION_POINTER_UP:
			touchState = NONE;
			return false;
		}

		synchronized (sync) {
			return !waitingForInput;
		}
	}

	/**
	 * Calculate distance between two fingers for touch events
	 * 
	 * @param event
	 * @return
	 */
	private float calcDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	public static final void onResume() {
        manager.registerListener(
            content, sensor_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public static final void onPause() {
	    manager.unregisterListener(content);
	}

	@Override
	public boolean onLongClick(View v) {
		synchronized (sync) {
			if (waitingForInput && waitingFor != waitingForDice) {
				player.waitedForInput(waitingForPegChosen, null);
				return long_press = true;
			} else
				return false;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		return;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (sync) {
			if (waitingForInput && waitingFor == waitingForDice
					&& event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				if (acceleration_x == 1000 || Math.abs(event.values[0] - acceleration_x) < 4
						|| Math.abs(event.values[1] - acceleration_y) < 4
						|| Math.abs(event.values[2] - acceleration_z) < 4) {
					acceleration_x = event.values[0];
					acceleration_y = event.values[1];
					acceleration_z = event.values[2];
				} else {
					acceleration_x = 1000;
					player.waitedForInput(waitingFor, event.values);
				}
		}
	}
}
