package de.tum;

import de.tum.player.HumanPlayer;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

public class GameTouchListener  implements OnTouchListener, OnLongClickListener {
	
    // Debugging
    private static final String TAG = "GameTouchListener";
    private static final boolean D = true;
	
    // Minimum distance between two fingers
    private final static float MIN_DIST = 50;
    // Distance between two fingers
    private static float eventDistance = 0;
    // possible touch states
    private final static int NONE = 0;
    private final static int DRAG = 1;
    private final static int ZOOM = 2;
    // current touch state
    private int touchState = NONE;

    private static boolean waitingForInput;
    public static final int waitingForDice = 0;
    public static final int waitingForPegSelection = 1;
    public static final int waitingForPegChosen = 2;
    private static int waitingFor;
    private static HumanPlayer player;
    private static boolean long_press;
    
    public synchronized static final void waitForInput(HumanPlayer player, int waitingFor) {
		waitingForInput = true;
		GameTouchListener.waitingFor = waitingFor;
		GameTouchListener.player = player;
    }
    
    public synchronized static final boolean isWaitingForInput() {
    	return waitingForInput;
    }
    
    public synchronized static final void stopWaiting() {
    	waitingForInput = false;
    }

    @Override
	public boolean onTouch(View v, MotionEvent event) {
    	switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            //primary touch event starts
            touchState = DRAG;
            break;

        case MotionEvent.ACTION_POINTER_DOWN:     	
            //secondary touch event starts: remember distance and center
            eventDistance = calcDistance(event);
            if (eventDistance > MIN_DIST) {
                touchState = ZOOM;
            }
            break;

        case MotionEvent.ACTION_MOVE:       	
            if (touchState == DRAG) {
                //single finger drag, use it when needed

            } else if (touchState == ZOOM) {
            	Log.d(TAG, "ZOOM");
                //multi-finger zoom, scale accordingly around center?
                float dist = calcDistance(event);

                if (dist > MIN_DIST) {
                    float scale = dist / eventDistance;
                    
//                  hz = f * event.getY() / height - f / 2;
//                  hz *= hz * Math.signum(hz);
                    MenschAergereDichNichtActivity.hz = scale;
                    MenschAergereDichNichtActivity.hz *= MenschAergereDichNichtActivity.hz * Math.signum(MenschAergereDichNichtActivity.hz);             
                }
            }
            break;

        case MotionEvent.ACTION_UP:
        	if (long_press)
        		return long_press = !long_press;
//            Log.d("listener", "tapp");
    		if (isWaitingForInput())
    			player.waitedForInput(waitingFor);
        case MotionEvent.ACTION_POINTER_UP:
            touchState = NONE;
            return false;
        }       

        return !waitingForInput; 
	}
	
    /**
     * Calculate distance between two fingers for touch events
     * @param event
     * @return
     */
    private float calcDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

	@Override
	public boolean onLongClick(View v) {
//        Log.d("listener", "long tap");
		if (isWaitingForInput() && waitingFor != waitingForDice) {
			player.waitedForInput(waitingForPegChosen);
			return long_press = true;
		}
		else
			return false;
	}

}
