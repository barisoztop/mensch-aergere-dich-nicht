package de.tum.multiplayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.GameRenderer;
import de.tum.GameTouchListener;
import de.tum.R;
import de.tum.Room;
import de.tum.Team;
import de.tum.bluetooth.BluetoothMPService;
import de.tum.bluetooth.DeviceListActivity;
import de.tum.models.Board;
import de.tum.models.ClassicBoard;
import de.tum.models.Dice;
import de.tum.player.AIPlayer;
import de.tum.player.HumanPlayer;
import de.tum.player.NetworkPlayer;
import de.tum.player.Player;

/**
 * main activity for controlling the game
 */
public class MultiplayerActivity extends Activity {

	// Debugging
	private static final String TAG = "MultiplayerActivity";
	private static final boolean D = true;

	/* Bluetooth communication */
	// Message types sent from the BluetoothMPService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_TITLE = 6;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String TITLE = "title";
	public static final String MAX_CLIENTS = "numberOfClients";
	// Intent request codes
	private static final int REQUEST_CONNECT_SERVER = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_MODE_TYPE = 3;
	
	
	public static final int RESULT_CLIENT_MODE = 1;
	public static final int RESULT_SERVER_MODE = 2;
	public static final int RESULT_GOBACK = 3;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothMPService mBluetoothMPService = null;
	// Name of the connected device
	private String mConnectedServerName = null;
	private String mConnectedDeviceName1 = null;
	private String mConnectedDeviceName2 = null;
	private String mConnectedDeviceName3 = null;

	// Layout Views
	private TextView titleBar;
	private ProgressDialog serverWaitingDialog;
	private static final int SERVER_WAITING_DIALOG = 0;
	
	private Intent modeSelectionIntent;
	private Intent clientNumberPickerIntent = null;
	public Handler modeSelectionHandler;
	private Bundle savedInstanceState;
	
	private int numberOfClients = 0;

	// just for testing
	// ############################### needs
	// some change
	private static final float f = 8;
	public static int width;
	public static int height;
	public static float hz = 4;
	private GLSurfaceView view;
	private GameRenderer renderer;
	// private Board board;
	private static Player[] players;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available, try New Game",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		/* setup game */
		new Room();
		Room.addRenderable(new ClassicBoard(true, 4));
		Room.addRenderable(new Dice(true));
		// players = new Player[Board.getPlayers()];
		// players[0] = new HumanPlayer(Team.RED, mHandler);
		// players[1] = new AIPlayer(Team.YELLOW);
		// players[2] = new AIPlayer(Team.GREEN);
		// players[3] = new AIPlayer(Team.BLUE);
		
		renderer = new GameRenderer();
		view = new GLSurfaceView(this);
		view.setRenderer(renderer);
		// GameTouchListener listener = new GameTouchListener();
		// view.setOnTouchListener(listener);
		// view.setOnLongClickListener(listener);

//		gameView = new GameView(getApplicationContext());
//		gameView.setRenderer(renderer);
		// Set up the window layout
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//	    requestWindowFeature(Window.FEATURE_PROGRESS);
		
//		final RelativeLayout rLayout = new RelativeLayout(this);
//		rLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 
//				LayoutParams.FILL_PARENT));
//		
//		RelativeLayout.LayoutParams gameView = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 
//				LayoutParams.FILL_PARENT);
//		
//		rLayout.addView(view, gameView);
		
//		serverWaitingDialog =  (ProgressDialog) findViewById(R.id.serverWaitingDialog);
//    	serverWaitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//    	serverWaitingDialog.setMessage("Waiting for others...");
		
		
		setContentView(view);
		
//		getWindow().addContentView(view, params)
//		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 0);

//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
//		titleBar = (TextView) findViewById(R.id.title_left_text);
//		titleBar.setText(R.string.app_name);
//		titleBar = (TextView) findViewById(R.id.title_right_text);
//		titleBar.setText("Multiplayer Mode Activity");

		// players[0].makeTurn();
		Log.d(TAG, "modeSelectionIntent");
		modeSelectionIntent = new Intent(this, ModeSelectionActivity.class);
		startActivityForResult(modeSelectionIntent, REQUEST_MODE_TYPE);
		Log.d(TAG, "modeSelectionIntent DONE");
		
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the communication between devices
		} else {
			if (mBluetoothMPService == null)
				setupMultiPlayer();
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
//		view.onPause();
		if (D) Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D) Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the BluetoothMPService
		if (mBluetoothMPService != null)
			mBluetoothMPService.stop();
		if (D) Log.e(TAG, "--- ON DESTROY ---");
	}

	@Override
	public void onResume() {
		super.onResume();
		view.onResume();

		if (D) Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
//		if (mBluetoothMPService != null) {
//			// Only if the state is STATE_NONE, do we know that we haven't
//			// started already
//			if (mBluetoothMPService.getState() == BluetoothMPService.STATE_NONE) {
//				// Start the Bluetooth chat services
//				mBluetoothMPService.start();
//			}
//		}
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
	 * The Handler that gets information back from the BluetoothMPService
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothMPService.STATE_ALL_CONNECTED:
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					Toast.makeText(getApplicationContext(), "STATE_ALL_CONNECTED",
							Toast.LENGTH_SHORT).show();
					MultiplayerActivity.this.startGame();
					break;
				case BluetoothMPService.STATE_CONNECTED_1:
				case BluetoothMPService.STATE_CONNECTED_2:
				case BluetoothMPService.STATE_CONNECTED_3:
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mChatService.connectedDevices +
					// " Device(s)");
					Toast.makeText(
							getApplicationContext(),
							mBluetoothMPService.connectedDevices
									+ " Device(s) connected",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothMPService.STATE_CONNECTED_TO_SERVER:
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mChatService.connectedDevices +
					// " Device(s)");
					Toast.makeText(getApplicationContext(),
							"Connected to Server: " + mConnectedServerName,
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothMPService.STATE_CONNECTING_TO_SERVER:
					// mTitle.setText(R.string.title_connecting);
					Toast.makeText(getApplicationContext(), "Connecting...",
							Toast.LENGTH_SHORT).show(); // TODO no need!
					break;
				case BluetoothMPService.STATE_LISTEN:
					break;
				case BluetoothMPService.STATE_NONE:
					// mTitle.setText(R.string.title_not_connected);
					Toast.makeText(getApplicationContext(), "Not Conntected",
							Toast.LENGTH_SHORT).show();
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				Log.d(TAG, "MESSAGE_WRITE - Message sent to other device(s)");
				
				// construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				Log.d(TAG, "MESSAGE_READ - Message came from other device(s)");
				byte[] readBuf = (byte[]) msg.obj;
				Toast.makeText(
						getApplicationContext(),
						"MESSAGE_READ - Message came from other device(s)",
						Toast.LENGTH_SHORT).show();
				// construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// int deviceID = msg.arg2;
				// mConversationArrayAdapter.add("Device "+ deviceID + ": " +
				// readMessage);
				MultiplayerActivity.this.convertArrivalData(readBuf);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				int currentState = mBluetoothMPService.getState();
				Log.d(TAG, "MESSAGE_DEVICE_NAME arrived: " + mBluetoothMPService.connectedDevices + " Devices and" + " Service State: " + currentState);
				if (currentState == BluetoothMPService.STATE_CONNECTED_1 ||
						currentState == BluetoothMPService.STATE_CONNECTED_2 ||
							currentState == BluetoothMPService.STATE_CONNECTED_3 ||
								currentState == BluetoothMPService.STATE_ALL_CONNECTED) {
					if (mBluetoothMPService.connectedDevices == 1) {
						Log.d(TAG, "mBluetoothMPService.connectedDevices == 1");
						mConnectedDeviceName1 = msg.getData().getString(
								DEVICE_NAME);
						Toast.makeText(getApplicationContext(),
								"Connected to " + mConnectedDeviceName1,
								Toast.LENGTH_SHORT).show();
						Log.d(TAG, "case MESSAGE_DEVICE_NAME & now setProgressValue");
						MultiplayerActivity.this.setProgessValue(1);

						
					} else if (mBluetoothMPService.connectedDevices == 2) {
						mConnectedDeviceName2 = msg.getData().getString(
								DEVICE_NAME);
						Toast.makeText(getApplicationContext(),
								"Connected to " + mConnectedDeviceName2,
								Toast.LENGTH_SHORT).show();
						MultiplayerActivity.this.setProgessValue(2);
					} else if (mBluetoothMPService.connectedDevices == 3) {
						mConnectedDeviceName3 = msg.getData().getString(
								DEVICE_NAME);
						Toast.makeText(getApplicationContext(),
								"Connected to " + mConnectedDeviceName3,
								Toast.LENGTH_SHORT).show();
						MultiplayerActivity.this.setProgessValue(3);
					}
				}

				if (mBluetoothMPService.getState() == BluetoothMPService.STATE_CONNECTED_TO_SERVER) {
					mConnectedServerName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(),
							"Connected to server " + mConnectedServerName,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_TITLE:
				titleBar.setText(msg.getData().getString(TITLE));
				break;
			}
		}
	};

	/**
	 * create menu when user click menu button
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
			startActivityForResult(serverIntent, REQUEST_CONNECT_SERVER);
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
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Handle all the onActivityResult events
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_SERVER:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mBluetoothMPService.connectServer(device);
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
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		case REQUEST_MODE_TYPE:
			if (resultCode == RESULT_CLIENT_MODE) {
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_CLIENT_MODE");
				// Launch the DeviceListActivity to see devices and do scan
				Intent connectServerIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(connectServerIntent, REQUEST_CONNECT_SERVER);
			} else if (resultCode == RESULT_SERVER_MODE){
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_SERVER_MODE");

				if (clientNumberPickerIntent == null) {
					Log.d(TAG, "clientNumberPickerIntent == null");
					clientNumberPickerIntent = new Intent(this, ClientNumberPicker.class);
//					clientNumberPickerIntent.putExtra(MAX_CLIENTS, numberOfClients);
					startActivityForResult(clientNumberPickerIntent, REQUEST_MODE_TYPE);
				} else {
					Log.d(TAG, "clientNumberPickerIntent != null");
					Log.d(TAG, "Old numberOfClients: " + numberOfClients);
					clientNumberPickerIntent = null;
					numberOfClients = data.getExtras().getInt(MAX_CLIENTS);
					mBluetoothMPService.setMaxDeviceNumber(numberOfClients);
					Log.d(TAG, "New numberOfClients: " + numberOfClients);
					
					if (mBluetoothMPService != null) {
						// Only if the state is STATE_NONE, do we know that we haven't
						// started already
						if (mBluetoothMPService.getState() == BluetoothMPService.STATE_NONE) {
							// Start the Bluetooth chat services
							mBluetoothMPService.startServer();
						}
					}
					
					Log.d(TAG, "Create Progress Dialog");
					MultiplayerActivity.this.serverWaitingDialog = new ProgressDialog(MultiplayerActivity.this);
					Log.d(TAG, "Create Progress Dialog Success!");
					MultiplayerActivity.this.serverWaitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					Log.d(TAG, "Create Progress Dialog setProgressStyle Success!");
					MultiplayerActivity.this.serverWaitingDialog.setMessage("Waiting for others...");
					Log.d(TAG, "Create Progress Dialog setMessage Success!");
					MultiplayerActivity.this.serverWaitingDialog.setCancelable(true);
					Log.d(TAG, "Create Progress Dialog setCancelable Success!");
					MultiplayerActivity.this.serverWaitingDialog.show();
					Log.d(TAG, "Create Progress Dialog Success!");
					
				}

			} else if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_CANCELED");
				finish();
			} else if (resultCode == RESULT_GOBACK) {
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_GOBACK");
				finish(); // TODO create MultiPlayer activity again
			}
		}
	}

	/**
	 * 
	 * Set the action listeners in here !!!!!!!!!! use sendMessage() to send
	 * your new object in here
	 */
	private void setupMultiPlayer() {
		Log.d(TAG, "setupMultiPlayer()");
		// TODO Modify

		// Initialize the BluetoothMPService to perform bluetooth connections
		mBluetoothMPService = new BluetoothMPService(this, mHandler);

		// Initialize the buffer for outgoing messages
		// mOutStringBuffer = new StringBuffer("");

	}

	/**
	 * Sends a message. This will be converted to any object that we wanna send
	 * !!!!!!!!
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(Object data) {
		// Check if all devices connected to server
		if (mBluetoothMPService.serverDevice
				&& mBluetoothMPService.getState() != mBluetoothMPService.STATE_ALL_CONNECTED) {
			Toast.makeText(this, R.string.waiting_others, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (!mBluetoothMPService.serverDevice
				&& mBluetoothMPService.getState() != mBluetoothMPService.STATE_CONNECTED_TO_SERVER) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Client connected to server but not all devices connected to server
		// TODO transmission is possible even now
		// if (!mChatService.serverDevice && mChatService.getState() !=
		// BluetoothChatService.STATE_CONNECTED_TO_SERVER) {
		// Toast.makeText(this, R.string.waiting_others,
		// Toast.LENGTH_SHORT).show();
		// return;
		// }

		// Check that there's actually something to send
		if (data != null) {
			// Get the message bytes and tell the BluetoothChatService to write
			ByteArrayOutputStream bos = null;
			ObjectOutput out = null;
			try {
				bos = new ByteArrayOutputStream();
				out = new ObjectOutputStream(bos);
				out.writeObject(data);
			} catch (IOException e) {
				e.printStackTrace();
			}   
			byte[] send = bos.toByteArray();
			mBluetoothMPService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			// mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
			
			try {
				if (out != null) out.close();
				if (bos != null) bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
    private void setProgessValue(int value) {
    	
    	int barValue = (100 / mBluetoothMPService.getMaxDeviceNumber()) * value;
    	Log.d(TAG, "setProgessValue()----> barValue: " + barValue);
		MultiplayerActivity.this.serverWaitingDialog.setProgress(barValue);
		Log.d(TAG, "setProgress(barValue) SUCCESS!");
        if ( barValue >= 100){
        	Log.d(TAG, "setProgress(barValue) SUCCESS!");
        	serverWaitingDialog.dismiss();
        	
        }
    	
    }
    
	private void startGame() {
		
		/* setup game */
		
//		view.invalidate();
//		view.refreshDrawableState();
		players = new Player[Board.getPlayers()];
		players[0] = new HumanPlayer(Team.RED, mHandler, MultiplayerActivity.class);
		players[1] = new AIPlayer(Team.YELLOW, MultiplayerActivity.class);
		players[2] = new AIPlayer(Team.GREEN, MultiplayerActivity.class);
		players[3] = new AIPlayer(Team.BLUE, MultiplayerActivity.class);
//		players[3] = new NetworkPlayer(Team.BLUE, MultiplayerActivity.class);
		
//		renderer = new GameRenderer();
//		view = new GLSurfaceView(this);
//		view.setRenderer(renderer);
		GameTouchListener listener = new GameTouchListener();
		view.setOnTouchListener(listener);
		view.setOnLongClickListener(listener);
//		setContentView(view);
//		view.refreshDrawableState();

		players[0].makeTurn();
		
		DataServer dataServer = new DataServer();
		dataServer.player = players;
		
		sendMessage(dataServer);
		
		
		
	}
	
	protected void convertArrivalData(byte[] readBuf) {
		ByteArrayInputStream bis = new ByteArrayInputStream(readBuf);
		ObjectInput in = null;
		Object o = null;
		try {
			in = new ObjectInputStream(bis);
			o = in.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (o != null && mBluetoothMPService.serverDevice) processArrivalClientData((DataClient) o);
		if (o != null && !mBluetoothMPService.serverDevice) processArrivalServerData((DataServer) o);

		try {
			if (bis != null) bis.close();
			if (in != null) in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}

	private void processArrivalClientData(DataClient object) {
		// TODO Auto-generated method stub
		
	}
	
	private void processArrivalServerData(DataServer object) {
		players = object.player;
		GameTouchListener listener = new GameTouchListener();
		view.setOnTouchListener(listener);
		view.setOnLongClickListener(listener);
		players[0].makeTurn();
		Toast.makeText(getApplicationContext(),
				"processArrivalServerData",
				Toast.LENGTH_SHORT).show();
	}	



}