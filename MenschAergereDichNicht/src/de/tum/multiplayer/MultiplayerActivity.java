package de.tum.multiplayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedList;

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
import de.tum.GameListener;
import de.tum.MenschAergereDichNichtActivity;
import de.tum.R;
import de.tum.Room;
import de.tum.Team;
import de.tum.models.Board;
import de.tum.models.ClassicBoard;
import de.tum.models.Dice;
import de.tum.multiplayer.bluetooth.BluetoothMPService;
import de.tum.multiplayer.bluetooth.DeviceListActivity;
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
	// Message types of the handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_TITLE = 6;

	// Key names for handler messages
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String TITLE = "title";
	public static final String MAX_CLIENTS = "numberOfClients";
	
	// Intent request codes
	private static final int REQUEST_CONNECT_SERVER = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_MODE_TYPE = 3;
	
	// Intent result codes
	public static final int RESULT_CLIENT_MODE = 1;
	public static final int RESULT_SERVER_MODE = 2;
	public static final int RESULT_GOBACK = 3;

	// Local Bluetooth adapter
	private BluetoothAdapter bluetoothAdapter = null;
	// Manage bluetooth communication
	private BluetoothMPService bluetoothMPService = null;
	// Name of the connected device
	private String connectedServerName = null;
	private String connectedDeviceName1 = null;
	private String connectedDeviceName2 = null;
	private String connectedDeviceName3 = null;
	private String connectedDeviceName4 = null;
	private String connectedDeviceName5 = null;
	private String connectedDeviceName6 = null;
	private String connectedDeviceName7 = null;

	// Layout members
	private TextView titleBar;
	private ProgressDialog serverWaitingDialog;
	
	// Intent to select the multiplayer mode
	private Intent modeSelectionIntent = null;
	// Intent to select the number of clients
	private Intent clientNumberPickerIntent = null;
	// Number of connected clients to server
	private int numberOfClients = 0;


	/* graphics members */
	private static final float f = 8;
	public static int width;
	public static int height;
	public static float hz = 4;
	private GLSurfaceView view;
	private GameRenderer renderer;
	// private Board board;
	private static Player[] players;
	private Room room;
	private static final LinkedList<int[]> tokens = new LinkedList<int[]>();
	
	private static MultiplayerActivity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		// Get local Bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (bluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available, try Single Player game",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		/* setup game */
		room = new Room();
		Room.addRenderable(new ClassicBoard(true, 4));
		Room.addRenderable(new Dice(true));
		 players = new Player[Board.getPlayers()];
//		 players[0] = new NetworkPlayer(Team.RED, MultiplayerActivity.class);
//		 players[1] = new HumanPlayer(Team.YELLOW, mHandler, MultiplayerActivity.class);
//		 players[2] = new HumanPlayer(Team.GREEN, mHandler, MultiplayerActivity.class);
//		 players[3] = new AIPlayer(Team.BLUE, MultiplayerActivity.class);
		
		renderer = new GameRenderer();
		view = new GLSurfaceView(this);
		view.setRenderer(renderer);
		GameListener listener = new GameListener(this);
		view.setOnTouchListener(listener);
		view.setOnLongClickListener(listener);
		
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		// Set up the custom title
		titleBar = (TextView) findViewById(R.id.title_left_text);
		titleBar.setText("Connection Status: ");
		titleBar = (TextView) findViewById(R.id.title_right_text);
		titleBar.setText("None");
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.e(TAG, "++ ON START ++");

		// If Bluetooth is not on, request enable.
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			// setup the multiplayer mode
			if (bluetoothMPService == null)
				setupMultiPlayer();
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		GameListener.onPause();
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
		if (bluetoothMPService != null)
			bluetoothMPService.stop(false);
		if (D) Log.e(TAG, "--- ON DESTROY ---");
	}

	@Override
	public void onResume() {
		super.onResume();
		view.onResume();
		GameListener.onResume();
		if (D) Log.e(TAG, "+ ON RESUME +");
	}

	/**
	 * it's the next player's turn. Calls the next player for its turn
	 * 
	 * @param team
	 *            the current team
	 */
	public static final void nextTurn(Team team) {
    	if (activity != null)
		players[(team.id + 1) % players.length].makeTurn();
    	else
    		MenschAergereDichNichtActivity.nextTurn(team);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if (bluetoothMPService != null)
				bluetoothMPService.stop(false);
			System.exit(0);
			finish();
			
		}
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
//					Toast.makeText(getApplicationContext(), "STATE_ALL_CONNECTED",
//							Toast.LENGTH_SHORT).show();
					// all devices are connected, so start the game
					titleBar.setText("All Connected");
					MultiplayerActivity.this.startGame();
					break;
				case BluetoothMPService.STATE_WAITING_FOR_CONNECTIONS:
					Toast.makeText(
							getApplicationContext(),
							bluetoothMPService.getConnectedDevices()
									+ " device(s) is connected.",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothMPService.STATE_CONNECTED_TO_SERVER:
					break;
				case BluetoothMPService.STATE_CONNECTING_TO_SERVER:
//					Toast.makeText(getApplicationContext(), "Connecting to server...",
//							Toast.LENGTH_SHORT).show();
					titleBar.setText("Connecting to server...");
					break;
				case BluetoothMPService.STATE_LISTEN:
					break;
				case BluetoothMPService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				// message that this device send to other(s)
				byte[] writeBuf = (byte[]) msg.obj;
				Log.d(TAG, "MESSAGE_WRITE - Message is sent to other device(s)");
				break;
			case MESSAGE_READ:
				Log.d(TAG, "MESSAGE_READ - Message came from other device(s)");
				byte[] readBuf = (byte[]) msg.obj;
				int deviceID = msg.arg2; // Device ID, -1 is the server
				MultiplayerActivity.this.convertArrivalData(readBuf);
				break;
			case MESSAGE_DEVICE_NAME:
				// set name of the connected device and
				// the progress dialog value after each connection
				int currentState = bluetoothMPService.getState();
				Log.d(TAG, "MESSAGE_DEVICE_NAME: " + bluetoothMPService.getConnectedDevices() + " Device(s) and" + " Service State: " + currentState);
				
				// Server got the clients connected message
				if (currentState == BluetoothMPService.STATE_WAITING_FOR_CONNECTIONS ||
						currentState == BluetoothMPService.STATE_ALL_CONNECTED) {
					if (bluetoothMPService.getConnectedDevices() == 1) {
						connectedDeviceName1 = msg.getData().getString(DEVICE_NAME);
						Toast.makeText(getApplicationContext(),
								"Connected to " + connectedDeviceName1,
								Toast.LENGTH_SHORT).show();
						Log.d(TAG, "case MESSAGE_DEVICE_NAME & now setProgressValue");
						MultiplayerActivity.this.setProgessValue(1);

					} else if (bluetoothMPService.getConnectedDevices() == 2) {
						connectedDeviceName2 = msg.getData().getString(DEVICE_NAME);
						Toast.makeText(getApplicationContext(),
								"Connected to " + connectedDeviceName2,
								Toast.LENGTH_SHORT).show();
						MultiplayerActivity.this.setProgessValue(2);

					} else if (bluetoothMPService.getConnectedDevices() == 3) {
						connectedDeviceName3 = msg.getData().getString(DEVICE_NAME);
						Toast.makeText(getApplicationContext(),
								"Connected to " + connectedDeviceName3,
								Toast.LENGTH_SHORT).show();
						MultiplayerActivity.this.setProgessValue(3);
					}
					 else if (bluetoothMPService.getConnectedDevices() == 4) {
						 connectedDeviceName4 = msg.getData().getString(DEVICE_NAME);
							Toast.makeText(getApplicationContext(),
									"Connected to " + connectedDeviceName4,
									Toast.LENGTH_SHORT).show();
							MultiplayerActivity.this.setProgessValue(4);
						}
					 else if (bluetoothMPService.getConnectedDevices() == 5) {
						 connectedDeviceName5 = msg.getData().getString(DEVICE_NAME);
							Toast.makeText(getApplicationContext(),
									"Connected to " + connectedDeviceName5,
									Toast.LENGTH_SHORT).show();
							MultiplayerActivity.this.setProgessValue(5);
						}
					 else if (bluetoothMPService.getConnectedDevices() == 6) {
						 connectedDeviceName6 = msg.getData().getString(DEVICE_NAME);
							Toast.makeText(getApplicationContext(),
									"Connected to " + connectedDeviceName6,
									Toast.LENGTH_SHORT).show();
							MultiplayerActivity.this.setProgessValue(6);
						}
					 else if (bluetoothMPService.getConnectedDevices() == 7) {
						 connectedDeviceName7 = msg.getData().getString(DEVICE_NAME);
							Toast.makeText(getApplicationContext(),
									"Connected to " + connectedDeviceName7,
									Toast.LENGTH_SHORT).show();
							MultiplayerActivity.this.setProgessValue(7);
						}
				}

				// Client got the connected to the server message
				if (currentState == BluetoothMPService.STATE_CONNECTED_TO_SERVER) {
					connectedServerName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(),
							"Connected to server " + connectedServerName,
							Toast.LENGTH_SHORT).show();
					titleBar.setText("@" + connectedServerName);
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
	 * create menu button's menu
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
		case R.id.menu_server_mode:
			onActivityResult(REQUEST_MODE_TYPE, RESULT_SERVER_MODE, null);
			return true;
		case R.id.menu_client_mode:
			onActivityResult(REQUEST_MODE_TYPE, RESULT_CLIENT_MODE, null);
			return true;			
		case R.id.menu_discovery:
			makeDiscoverable();
			return true;
		}
		return false;
	}

	/**
	 * Ensure if the device is discoverable by others for 300 sec
	 */
	private void makeDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Handle all onActivityResult events
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_SERVER:
			// Clients connects to DeviceListActivity's server
			if (resultCode == Activity.RESULT_OK) {
				// Get the server's MAC
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the server's BLuetoothDevice object
				BluetoothDevice serverDevice = bluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to server
				bluetoothMPService.connectServer(serverDevice);
				startClientGame(); // TODO check states
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				// setup multiplayer mode
				setupMultiPlayer();
			} else {
				// Bluetooth isn't enabled or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.try_singleplayer,
						Toast.LENGTH_LONG).show();
				finish();
			}
		case REQUEST_MODE_TYPE:
			if (resultCode == RESULT_CLIENT_MODE) {
				// Device is client, select server device to connect
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_CLIENT_MODE");
				Intent connectServerIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(connectServerIntent, REQUEST_CONNECT_SERVER);
			} else if (resultCode == RESULT_SERVER_MODE){
				// Device is server
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_SERVER_MODE");
				if (clientNumberPickerIntent == null) {
					// Select the number of clients
					Log.d(TAG, "clientNumberPickerIntent == null");
					clientNumberPickerIntent = new Intent(this, ClientNumberPicker.class);
					clientNumberPickerIntent.putExtra(MAX_CLIENTS, numberOfClients);
					startActivityForResult(clientNumberPickerIntent, REQUEST_MODE_TYPE);
				} else {
					// set the selected clients amount
					Log.d(TAG, "clientNumberPickerIntent != null");
					clientNumberPickerIntent = null;
					numberOfClients = data.getExtras().getInt(MAX_CLIENTS);
					bluetoothMPService.setMaxDeviceNumber(numberOfClients);
					Log.d(TAG, "numberOfClients: " + numberOfClients);
					
					if (bluetoothMPService != null) {
						if (bluetoothMPService.getState() == BluetoothMPService.STATE_NONE) {
							// Start server listening mode
							bluetoothMPService.startServer();
						}
					}
					// Create progress dialog to show the device connection activity
					MultiplayerActivity.this.serverWaitingDialog = new ProgressDialog(MultiplayerActivity.this);
					MultiplayerActivity.this.serverWaitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					MultiplayerActivity.this.serverWaitingDialog.setMessage(getResources().getText(R.string.waiting_others));
					MultiplayerActivity.this.serverWaitingDialog.setCancelable(true);
					MultiplayerActivity.this.serverWaitingDialog.show();
					Log.d(TAG, "Creating Progress Dialog is successful!");
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
	 * Initiate the BluetoothMPService and select game mode 
	 */
	private void setupMultiPlayer() {
		Log.d(TAG, "setupMultiPlayer()");
		bluetoothMPService = new BluetoothMPService(this, mHandler);
		modeSelectionIntent = new Intent(this, ModeSelectionActivity.class);
		startActivityForResult(modeSelectionIntent, REQUEST_MODE_TYPE);
	}
	
	/**
	 * Sends the object to other device(s) for both client
	 * and server side
	 * 
	 * @param data Object that is sent
	 */
	private void sendMessage(Object data) {
		// If server, check if all devices connected to server
		if (bluetoothMPService.serverDevice
				&& bluetoothMPService.getState() != BluetoothMPService.STATE_ALL_CONNECTED) {
			return;
		}

		// If client, check if it's connected to server
		if (!bluetoothMPService.serverDevice
				&& bluetoothMPService.getState() != BluetoothMPService.STATE_CONNECTED_TO_SERVER) {
			return;
		}

		// Check there is data to send
		if (data != null) {
			// Convert the Object to byte[]
			ByteArrayOutputStream bos = null;
			ObjectOutput out = null;
			try {
				bos = new ByteArrayOutputStream();
				out = new ObjectOutputStream(bos);
				out.writeObject(data);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}   
			byte[] send = bos.toByteArray();
			// send the data
			bluetoothMPService.write(send);
		}
	}
	
	/**
	 * Set the value of the progress bar when server device
	 * is waiting for client devices to connect
	 * @param value Number of devices connected
	 */
    private void setProgessValue(int value) {
    	// convert the value to percentage
    	double barValue = ((100 / bluetoothMPService.getMaxDeviceNumber()) + 0.5) * value;
    	Log.d(TAG, "setProgessValue()----> barValue: " + barValue);
		MultiplayerActivity.this.serverWaitingDialog.setProgress((int) barValue);
		Log.d(TAG, "setProgress(barValue) SUCCESS!");
        if ( barValue >= 100){
        	// all connected
        	serverWaitingDialog.dismiss();
        }
    }
    
    public static final void notifyPlayers(int[] tokens) {
    	if (activity != null)
		activity.sendMessage(new DataServer(tokens));
    }
    
    /**
     * Initialize the player on the board and start the first movement 
     */
	private void startGame() {
		Log.d(TAG, "startGame()");
		players = new Player[Board.getPlayers()];
		 players[0] = new HumanPlayer(Team.RED, mHandler);
		 players[1] = new HumanPlayer(Team.YELLOW, mHandler);
		 players[2] = new HumanPlayer(Team.GREEN, mHandler);
		 players[3] = new HumanPlayer(Team.BLUE, mHandler);

		Toast.makeText(getApplicationContext(), "server",
				Toast.LENGTH_LONG).show();

		players[0].makeTurn();
	}
	
	private void startClientGame() {
		players[0] = new NetworkPlayer(Team.RED);
		players[1] = new NetworkPlayer(Team.YELLOW);
		players[2] = new NetworkPlayer(Team.GREEN);
		players[3] = new NetworkPlayer(Team.BLUE);
		players[0].makeTurn();
	}

	/**
	 * Convert data arrived from other device to corresponding
	 * object according to the device mode
	 * 
	 * @param readBuf data arrived from other device
	 */
	protected void convertArrivalData(byte[] readBuf) {
		Log.d(TAG, "convertArrivalData()");
		ByteArrayInputStream bis = null;
		ObjectInput in = null;
		Object o = null;
		try {
			bis = new ByteArrayInputStream(readBuf);
			in = new ObjectInputStream(bis);
			o = in.readObject();
			in.close();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// process the data according to device mode
		if (o != null && bluetoothMPService.serverDevice) processArrivalClientData((DataClient) o);
		if (o != null && !bluetoothMPService.serverDevice) processArrivalServerData((DataServer) o);
	}

	/**
	 * Process data arrived from client to server
	 * 
	 * @param object
	 */
	private void processArrivalClientData(DataClient object) {
		Log.d(TAG, "processArrivalServerData()");
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Process data arrived from server to client
	 * 
	 * @param object
	 */
	private void processArrivalServerData(DataServer object) {
		Log.d(TAG, "processArrivalServerData() --");
//		players = object.player;
//		GameTouchListener listener = new GameTouchListener();
//		view.setOnTouchListener(listener);
//		view.setOnLongClickListener(listener);
//		players[0].makeTurn();
		
//		Toast.makeText(getApplicationContext(),
//				"processArrivalServerData: " + object,
//				Toast.LENGTH_SHORT).show();
		addToken(object.tokens);
			Toast.makeText(getApplicationContext(),
					"processArrivalServerData: " +
			(object.tokens[0] == NetworkPlayer.DICE_THROWN ? "dice thrown: " : "peg moved: ")
			+ object.tokens[1], Toast.LENGTH_SHORT).show();
//		Toast.makeText(getApplicationContext(),
//				"processArrivalServerData: " +
//		(object.tokens[0] == NetworkPlayer.DICE_THROWN ? "dice thrown " + object.tokens[1] : "error"),
//				Toast.LENGTH_SHORT).show();
		
	
//		GameTouchListener listener = new GameTouchListener();
//		view.setOnTouchListener(listener);
//		view.setOnLongClickListener(listener);
	}	

    public static final void tokenDone() {
//    	Log.d("multiplayer", "token done");
    	if (tokens != null)
	    	synchronized (tokens) {
	    		if(!tokens.isEmpty())
	    		  tokens.remove();
	    		if (!tokens.isEmpty())
	    			NetworkPlayer.notify(tokens.element());
	    	}
    }
    
    private static final void addToken(int[] token) {
//    	Log.d("multiplayer", "add token");
    	if (tokens != null)
	    	synchronized (tokens) {
		    	tokens.add(token);
				if (tokens.size() == 1)
					NetworkPlayer.notify(tokens.element());
	    	}
    }
}