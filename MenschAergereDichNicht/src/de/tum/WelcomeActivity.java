package de.tum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class WelcomeActivity extends Activity{
	
    // Debugging
    private static final String TAG = "WelcomeActivity";
    private static final boolean D = true;
	
	private Button newGameButton;
	private Button multiplayerGameButton;
	private Button settingsButton;
	private Button aboutButton;
	private Button exitButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        
        // Initialize the buttons
        newGameButton = (Button) findViewById(R.id.newgame);
        multiplayerGameButton = (Button) findViewById(R.id.multiplayer);
        settingsButton = (Button) findViewById(R.id.settings);
        aboutButton = (Button) findViewById(R.id.about);
        exitButton = (Button) findViewById(R.id.exit);
        
        newGameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent newGame = new Intent(getApplicationContext(), MenschAergereDichNichtActivity.class);
				startActivity(newGame);
				
			}
		});
        
        multiplayerGameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent multiplayerGame = new Intent(getApplicationContext(), MultiplayerActivity.class);
				startActivity(multiplayerGame);
				
			}
		});
        
        settingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        aboutButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        exitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
    }
    
    @Override
    public void onStart() {
      super.onStart();
      if(D) Log.e(TAG, "- ON START -");      
    }
    
    @Override
    public void onResume() {
      super.onResume();
      if(D) Log.e(TAG, "- ON RESUME -"); 
    }
    
    @Override
    public void onPause() {
      super.onPause();
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
}
