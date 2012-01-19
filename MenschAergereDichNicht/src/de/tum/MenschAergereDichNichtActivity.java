package de.tum;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import de.tum.bluetooth.BluetoothMPService;
import de.tum.bluetooth.DeviceListActivity;
import de.tum.models.Board;
import de.tum.models.ClassicBoard;
import de.tum.models.Dice;
import de.tum.player.AIPlayer;
import de.tum.player.HumanPlayer;
import de.tum.player.Player;

/**
 * main activity for controlling the game
 */
public class MenschAergereDichNichtActivity extends Activity {
	
    // Debugging
    private static final String TAG = "MenschAergereDichNicht";
    private static final boolean D = true;
    
    public static final int MESSAGE_TOAST = 5;
    public static final String TOAST = "toast";
	
	// just for testing
	// ############################### needs
	// some change
    private static final float f = 8;
    public static int width;
    public static int height;
    public static float hz = 4;
    private GLSurfaceView view;
    private GameRenderer renderer;
    private Room room;
//    private Board board;
    private static Player[] players;
    private static MenschAergereDichNichtActivity context;



    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      context = this;
      if(D) Log.e(TAG, "+++ ON CREATE +++");      
      
      room = new Room();

      Room.addRenderable(new ClassicBoard(true, 4));
      Room.addRenderable(new Dice(true));
      players = new Player[Board.getPlayers()];
      players[0] = new HumanPlayer(Team.RED, mHandler);
      players[1] = new AIPlayer(Team.YELLOW);
      players[2] = new AIPlayer(Team.GREEN);
      players[3] = new AIPlayer(Team.BLUE);
      
      renderer = new GameRenderer(room, this);
      view = new GLSurfaceView(this);
      view.setRenderer(renderer);
      GameTouchListener listener = new GameTouchListener(); 
      view.setOnTouchListener(listener);
      view.setOnLongClickListener(listener);
      setContentView(view);
      
      players[0].makeTurn();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
    }    
    
    @Override
    public synchronized void onPause() {
      super.onPause();
      view.onPause();
      if(D) Log.e(TAG, "- ON PAUSE -");      
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }    

    @Override
    public void onResume() {
      super.onResume();
      view.onResume();      
      if(D) Log.e(TAG, "+ ON RESUME +");
    }
    
	/**
	 * it's the next player's turn. Calls the next player for its turn
	 * 
	 * @param team
	 *            the current team
	 */
    public static final void nextTurn(Team team) {
    	players[(team.id + 1) % players.length].makeTurn();
    }

//    public static final void showMessage(String message) {
////		context.makeToast(message, show_long);
//		Message msg = context.mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(MenschAergereDichNichtActivity.TOAST, message);
//        msg.setData(bundle);
//        context.mHandler.sendMessage(msg);
//    }
//    
//    private final void makeToast(String message, boolean show_long) {
//    	Toast.makeText(getApplicationContext(), message, show_long ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK)
        System.exit(0);
      else
        return false;
      return true;
    }
    
    /**
     *  The Handler that gets information back from the BluetoothMPService
     */
    private final Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

}