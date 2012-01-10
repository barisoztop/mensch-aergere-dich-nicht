package de.tum;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * main activity for controlling the game
 */
public class MenschAergereDichNichtActivity extends Activity implements OnTouchListener {
	// just for testing
	// ############################### needs
	// some change
    private static final float f = 8;
    public static int width;
    public static int height;
    public static float hz;
    private GLSurfaceView view;
    private GameRenderer renderer;
    private Room room;
//    private Board board;
    private static Player[] players;

    public boolean onTouch(View view, MotionEvent event) {
      hz = f * event.getY() / height - f / 2;
      hz *= hz * Math.signum(hz);
      return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      room = new Room();

      Room.addRenderable(new ClassicBoard(true, 4));
      players = new Player[Board.getPlayers()];
      players[0] = new HumanPlayer(Team.RED);
      players[1] = new AIPlayer(Team.YELLOW);
      players[2] = new AIPlayer(Team.GREEN);
      players[3] = new AIPlayer(Team.BLUE);
      
      renderer = new GameRenderer(room, this);
      view = new GLSurfaceView(this);
      view.setRenderer(renderer);
      view.setOnTouchListener(this);
      setContentView(view);
      
      players[0].makeTurn();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK)
        System.exit(0);
      else
        return false;
      return true;
    }

    @Override
    public void onPause() {
      super.onPause();
      view.onPause();
    }

    @Override
    public void onResume() {
      super.onResume();
      view.onResume();
    }
}