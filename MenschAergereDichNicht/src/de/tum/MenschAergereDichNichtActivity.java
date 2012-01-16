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
    private static final boolean BLUETOOTH = true;
    
    /* Bluetooth communication */
    // Message types sent from the BluetoothMPService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothMPService mBluetoothMPService = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Name of the connected device
    private String mConnectedServerName = null;
    private String mConnectedDeviceName1 = null;
    private String mConnectedDeviceName2 = null;
    private String mConnectedDeviceName3 = null;

	
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



    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if(D) Log.e(TAG, "+++ ON CREATE +++");      
      
      if (BLUETOOTH) {
	      // Get local Bluetooth adapter
	      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	      // If the adapter is null, then Bluetooth is not supported
	      if (mBluetoothAdapter == null) {
	          Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	          finish();
	          return;
	      }
      }
      
      room = new Room();

      Room.addRenderable(new ClassicBoard(true, 4));
      Room.addRenderable(new Dice(true));
      players = new Player[Board.getPlayers()];
      players[0] = new HumanPlayer(Team.RED);
      players[1] = new AIPlayer(Team.YELLOW);
      players[2] = new AIPlayer(Team.GREEN);
      players[3] = new AIPlayer(Team.BLUE);
      
      renderer = new GameRenderer(room, this);
      view = new GLSurfaceView(this);
      view.setRenderer(renderer);
      view.setOnTouchListener(new GameTouchListener());
      setContentView(view);
      
      players[0].makeTurn();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        if (BLUETOOTH) {
	        // If BT is not on, request that it be enabled.
	        // setupChat() will then be called during onActivityResult
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        // Otherwise, setup the communication between devices
	        } else {
	            if (mBluetoothMPService == null) setupMultiPlayer();
	        }
        }
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
        // Stop the BluetoothMPService
        if (mBluetoothMPService != null) mBluetoothMPService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }    

    @Override
    public void onResume() {
      super.onResume();
      view.onResume();
      
      if(D) Log.e(TAG, "+ ON RESUME +");

      // Performing this check in onResume() covers the case in which BT was
      // not enabled during onStart(), so we were paused to enable it...
      // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
      if (mBluetoothMPService != null) {
          // Only if the state is STATE_NONE, do we know that we haven't started already
          if (mBluetoothMPService.getState() == BluetoothMPService.STATE_NONE) {
            // Start the Bluetooth chat services
        	  mBluetoothMPService.start();
          }
      }
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
    
    /**
     *  The Handler that gets information back from the BluetoothMPService
     */
    private final Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothMPService.STATE_ALL_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
//                    mConversationArrayAdapter.clear();
                	Toast.makeText(getApplicationContext(), "Multiplayer Mode",
                            Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothMPService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mChatService.connectedDevices + " Device(s)");
                	Toast.makeText(getApplicationContext(), mBluetoothMPService.connectedDevices + " Device(s) connected",
                            Toast.LENGTH_SHORT).show();
                    break;                    
                case BluetoothMPService.STATE_CONNECTED_TO_SERVER:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mChatService.connectedDevices + " Device(s)");
                	Toast.makeText(getApplicationContext(), "Connected to Server" + mConnectedServerName,
                            Toast.LENGTH_SHORT).show();
                    break;                    
                case BluetoothMPService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                	Toast.makeText(getApplicationContext(), "Connecting...",
                            Toast.LENGTH_SHORT).show();  
                    break;
                case BluetoothMPService.STATE_LISTEN:
                	break;
                case BluetoothMPService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
                	Toast.makeText(getApplicationContext(), "Not Conntected",
                            Toast.LENGTH_SHORT).show();                	
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
//                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
//                String readMessage = new String(readBuf, 0, msg.arg1);
//                int deviceID = msg.arg2;
//                mConversationArrayAdapter.add("Device "+ deviceID + ": " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	if (mBluetoothMPService.getState() == BluetoothMPService.STATE_CONNECTED) {
	   	           	 if (mBluetoothMPService.connectedDevices == 1) {
	 	           		mConnectedDeviceName1 = msg.getData().getString(DEVICE_NAME);
	 	                   Toast.makeText(getApplicationContext(), "Connected to "
	 	                                  + mConnectedDeviceName1, Toast.LENGTH_SHORT).show();
	 	           	} else if ( mBluetoothMPService.connectedDevices == 2) {
	 	           		mConnectedDeviceName2 = msg.getData().getString(DEVICE_NAME);
	 	                Toast.makeText(getApplicationContext(), "Connected to "
	 	                               + mConnectedDeviceName2, Toast.LENGTH_SHORT).show();
	 	           	} else if ( mBluetoothMPService.connectedDevices == 3) {
	 	           		mConnectedDeviceName3 = msg.getData().getString(DEVICE_NAME);
	 	                Toast.makeText(getApplicationContext(), "Connected to "
	 	                               + mConnectedDeviceName3, Toast.LENGTH_SHORT).show();
	 	           	}
            	}
            	
            	if (mBluetoothMPService.getState() == BluetoothMPService.STATE_CONNECTED_TO_SERVER) {
	           		mConnectedServerName = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to server "
	                               + mConnectedServerName, Toast.LENGTH_SHORT).show();
	           	}
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
    /**
     *  create menu when user click menu button
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    /**
     * create menu options
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    
    /**
     * Ensure if the device is discoverable
     */
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    /**
     * Handle all the onActivityResult events
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mBluetoothMPService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a MP session
                setupMultiPlayer();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * 
     * Set the action listeners in here !!!!!!!!!!
     * use sendMessage() to send your new object in here 
     */
	private void setupMultiPlayer() {
        Log.d(TAG, "setupMultiPlayer()");
		// TODO Modify
		
        // Initialize the BluetoothMPService to perform bluetooth connections
		mBluetoothMPService = new BluetoothMPService(this, mHandler);

        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
		
	}
	
    /**
     * Sends a message. This will be converted to any object that
     * we wanna send !!!!!!!!
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
    	// Check if all devices connected to server
        if (mBluetoothMPService.serverDevice && mBluetoothMPService.getState() != mBluetoothMPService.STATE_ALL_CONNECTED) {
            Toast.makeText(this, R.string.waiting_others, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mBluetoothMPService.serverDevice && mBluetoothMPService.getState() != mBluetoothMPService.STATE_CONNECTED_TO_SERVER) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Client connected to server but not all devices connected to server TODO transmission is possible even now
//      if (!mChatService.serverDevice && mChatService.getState() != BluetoothChatService.STATE_CONNECTED_TO_SERVER) {
//          Toast.makeText(this, R.string.waiting_others, Toast.LENGTH_SHORT).show();
//          return;
//      }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothMPService.write(send);

            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }
}