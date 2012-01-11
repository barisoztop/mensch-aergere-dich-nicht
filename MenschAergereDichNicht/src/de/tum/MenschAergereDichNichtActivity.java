package de.tum;

import de.tum.bluetooth.BluetoothMPService;
import de.tum.bluetooth.DeviceListActivity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

/**
 * main activity for controlling the game
 */
public class MenschAergereDichNichtActivity extends Activity implements OnTouchListener {
	
    // Debugging
    private static final String TAG = "MenschADN";
    private static final boolean D = true;
    
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

    // Name of the connected device
    private String mConnectedDeviceName = null;    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothMPService mBluetoothMPService = null;
	
	
	
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
      if(D) Log.e(TAG, "+++ ON CREATE +++");      
      
      // Get local Bluetooth adapter
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

      // If the adapter is null, then Bluetooth is not supported
      if (mBluetoothAdapter == null) {
          Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
          finish();
          return;
      }
      
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
    
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

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
    
    @Override
    public void onPause() {
      super.onPause();
      view.onPause();
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
                case BluetoothMPService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
//                    mConversationArrayAdapter.clear();
                	Toast.makeText(getApplicationContext(), "Conntected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothMPService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                	Toast.makeText(getApplicationContext(), "Connecting...",
                            Toast.LENGTH_SHORT).show();  
                    break;
                case BluetoothMPService.STATE_LISTEN:
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
//                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
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
                // Bluetooth is now enabled, so set up a chat session
                setupMultiPlayer();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	private void setupMultiPlayer() {
		// TODO Auto-generated method stub
		
        // Initialize the BluetoothChatService to perform bluetooth connections
		mBluetoothMPService = new BluetoothMPService(this, mHandler);

        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
		
	}
}