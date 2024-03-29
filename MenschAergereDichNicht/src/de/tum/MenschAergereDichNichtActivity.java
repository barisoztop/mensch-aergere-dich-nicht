package de.tum;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.models.Board;
import de.tum.multiplayer.TeamMatching;
import de.tum.player.AIPlayer;
import de.tum.player.HumanPlayer;
import de.tum.player.Player;

/**
 * main activity for controlling the game
 */
public class MenschAergereDichNichtActivity extends Activity {
    // Debugging
    private static final String TAG = "MenschAergereDichNicht";
    private static final boolean D = false;
    
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_TITLE = 6;    
    public static final String TOAST = "toast";
    public static final String TITLE = "title";
	
	// game values
    private GLSurfaceView view;
    private GameRenderer renderer;
    private static Player[] players;
    private static MenschAergereDichNichtActivity context;
    
    // Layout Views
    private View toastLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContext(this);
      if(D) Log.e(TAG, "+++ ON CREATE +++");      
      
	  int teams[] = getIntent().getExtras().getIntArray(TeamMatching.key);
      players = new Player[Board.getTeams()];
      int playing = 4;
      for (int i = 0; i < teams.length; ++i)
    	  if (teams[i] == TeamMatching.int_disabled) {
    		  --playing;
    		  continue;
    	  }
    	  else if (teams[i] == TeamMatching.int_human)
    		  players[i] = new HumanPlayer(Team.getById(i));
    	  else
    		  players[i] = new AIPlayer(Team.getById(i), teams[i] - TeamMatching.offset_strategy);
      
      renderer = new GameRenderer();
      view = new GLSurfaceView(this);
      view.setRenderer(renderer);
      new GameListener(this); 
      setContentView(view);

      // Toast layout
      LayoutInflater inflater = getLayoutInflater();
      toastLayout = inflater.inflate(R.layout.toast_layout,
    		  (ViewGroup) findViewById(R.id.toast_layout_root));
      
      Board.startGame(playing);
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
      GameListener.onPause(view);
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
      GameListener.onResume(view);
      if(D) Log.e(TAG, "+ ON RESUME +");
    }
    
	/**
	 * displays a toast according to the given string value (resources)
	 * 
	 * @param toast
	 *            the int id
	 */
	public static final void showToast(int toast) {
	    Message msg = getContext().mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MenschAergereDichNichtActivity.TOAST, getContext().getString(toast));
        msg.setData(bundle);
        getContext().mHandler.sendMessage(msg);
	}

	private long lastBackPressTime = 0;
	private Toast toast;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			
			if (this.lastBackPressTime < System.currentTimeMillis() - 3000) {
				toast = Toast.makeText(this, 
						getString(R.string.press_again), 3000);
				toast.show();
				this.lastBackPressTime = System.currentTimeMillis();
			} else {
				if (toast != null)
				    toast.cancel();
				System.exit(0);
			}

		}
		else
			return false;
		return true;
	}

	/** getting the context */
    public static MenschAergereDichNichtActivity getContext() {
		return context;
	}

	/** setting the context */
	public static void setContext(MenschAergereDichNichtActivity context) {
		MenschAergereDichNichtActivity.context = context;
	}

	/**
     *  The Handler that gets information back from the BluetoothMPService
     */
    private final Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_TOAST:
				TextView text = (TextView) toastLayout.findViewById(R.id.toast_text);
				text.setText(msg.getData().getString(TOAST));

				Toast toast = new Toast(MenschAergereDichNichtActivity.this);
				toast.setGravity(Gravity.BOTTOM, 0, 60);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setView(toastLayout);
				toast.show();
                break;
            case MESSAGE_TITLE:
//            	titleBar.setText(msg.getData().getString(TITLE));
                break;                 
            }
        }
    };

}