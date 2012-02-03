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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.GameListener;
import de.tum.GameRenderer;
import de.tum.MenschAergereDichNichtActivity;
import de.tum.R;
import de.tum.Room;
import de.tum.Team;
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
	// Message types of the handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_TOAST_WARNING = 6;
	public static final int MESSAGE_TITLE = 7;

	// Key names for handler messages
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String TITLE = "title";
	public static final String MAX_CLIENTS = "numberOfClients";
	
	// Intent request codes
	private static final int REQUEST_CONNECT_SERVER = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_MODE_TYPE = 3;
	private static final int REQUEST_SET_PLAYERS = 4;
	
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
	private ProgressDialog waitingDialog;
	private ProgressDialog waitingTeamSettings;
	
	// Intent to select the multiplayer mode
	private Intent modeSelectionIntent = null;
	// Intent to select the number of clients
	private Intent clientNumberPickerIntent = null;
	// Number of connected clients to server
	private int numberOfClients = 0;
	
	private View toastLayout;
	private boolean dialogOpened = true;

	/* graphics members */
	private GLSurfaceView view;
	private GameRenderer renderer;
	private static Player[] players;
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
			Toast.makeText(this, getString(R.string.bluetooth_not_supported),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		/* setup game */
		Room.addRenderable(new ClassicBoard(true));
		Room.addRenderable(new Dice(true));
		players = new Player[Board.getPlayers()];
		
		renderer = new GameRenderer();
		view = new GLSurfaceView(this);
		view.setRenderer(renderer);
		new GameListener(this);
		
		// Set up the window layout for custom title
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		// Set up the custom title
		titleBar = (TextView) findViewById(R.id.title_left_text);
		titleBar.setText("Connection Status: ");
		titleBar = (TextView) findViewById(R.id.title_right_text);
		titleBar.setText("None");
		
		// Toast layout for game directives
		LayoutInflater inflater = getLayoutInflater();
		toastLayout = inflater.inflate(R.layout.toast_layout,
		                               (ViewGroup) findViewById(R.id.toast_layout_root));
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
		GameListener.onPause(view);
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
			bluetoothMPService.stop(true);
		if (D) Log.e(TAG, "--- ON DESTROY ---");
	}

	@Override
	public void onResume() {
		super.onResume();
		view.onResume();
		GameListener.onResume(view);
		if (D) Log.e(TAG, "+ ON RESUME +");
	}

	public static final void showToast(int toast) {
		if (activity == null)
			MenschAergereDichNichtActivity.showToast(toast);
		else {
		    Message msg = activity.mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(MenschAergereDichNichtActivity.TOAST, activity.getString(toast));
	        msg.setData(bundle);
	        activity.mHandler.sendMessage(msg);
		}
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
				if (bluetoothMPService != null)
					bluetoothMPService.stop(true);
				System.exit(0);
				finish();
			}

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
				if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothMPService.STATE_ALL_CONNECTED:
					// all devices are connected, so start the game
					titleBar.setText("All Connected");
					Log.d("server", "" + connectedServerName);
					Log.d("client1", "" + connectedDeviceName1);
					Log.d("client2", "" + connectedDeviceName2);
					Log.d("client3", "" + connectedDeviceName3);
					Log.d("client4", "" + connectedDeviceName4);
					Log.d("client5", "" + connectedDeviceName5);
					Log.d("client6", "" + connectedDeviceName6);
					Log.d("client7", "" + connectedDeviceName7);
					String devices[] = new String[bluetoothMPService.getConnectedDevices()];
					int index = 0;
					if (connectedDeviceName1 != null)
						devices[index++] = connectedDeviceName1;
					if (connectedDeviceName2 != null)
						devices[index++] = connectedDeviceName2;
					if (connectedDeviceName3 != null)
						devices[index++] = connectedDeviceName3;
					if (connectedDeviceName4 != null)
						devices[index++] = connectedDeviceName4;
					if (connectedDeviceName5 != null)
						devices[index++] = connectedDeviceName5;
					if (connectedDeviceName6 != null)
						devices[index++] = connectedDeviceName6;
					if (connectedDeviceName7 != null)
						devices[index++] = connectedDeviceName7;
					
					// Send the progress dialog's value to the client
					MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_SETTINGS,
							null), BluetoothMPService.ALL_DEVICES);
						
					startActivityForResult(new Intent(getApplicationContext(),
							TeamMatching.class).putExtra(
							TeamMatching.key, devices), REQUEST_SET_PLAYERS);
					break;
				case BluetoothMPService.STATE_WAITING_FOR_CONNECTIONS:
					break;
				case BluetoothMPService.STATE_CONNECTED_TO_SERVER:
					break;
				case BluetoothMPService.STATE_CONNECTING_TO_SERVER:
					titleBar.setText(getString(R.string.title_connecting_to_server));
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
				if (D) Log.d(TAG, "MESSAGE_WRITE - Message is sent to other device(s)");
				break;
			case MESSAGE_READ:
				if (D) Log.d(TAG, "MESSAGE_READ - Message came from other device(s)");
				byte[] readBuf = (byte[]) msg.obj;
				int deviceNo = msg.arg2; // -1 is the server
				MultiplayerActivity.this.convertArrivalData(readBuf, deviceNo);
				break;
			case MESSAGE_DEVICE_NAME:
				// set name of the connected device and
				// the progress dialog value after each connection
				int currentState = bluetoothMPService.getState();
				if (D) Log.d(TAG, "MESSAGE_DEVICE_NAME: " + bluetoothMPService.getConnectedDevices() + 
						" Device(s) and" + " Service State: " + currentState);
				
				// Server got the clients connected message
				if (currentState == BluetoothMPService.STATE_WAITING_FOR_CONNECTIONS ||
						currentState == BluetoothMPService.STATE_ALL_CONNECTED) {
					// data to send the clients about the connection status
					int[] conStatus = null;
					String conenctedDeviceName = msg.getData().getString(DEVICE_NAME);
					switch (bluetoothMPService.getConnectedDevices()) {
					case 1:
						// Get the client name
						connectedDeviceName1 = conenctedDeviceName;
						// Toast the client name
						toastConnectedDevice(connectedDeviceName1);
						// Change the progress dialog's value
						MultiplayerActivity.this.setProgessValue(1);
						// Send the progress dialog's value to the client
						if (bluetoothMPService.getMaxDeviceNumber() != 1) {
							conStatus = new int[]{bluetoothMPService.getMaxDeviceNumber(), 1};
							MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_WAITING,
									conStatus), BluetoothMPService.ALL_DEVICES);
						}
						break;
					case 2:
						connectedDeviceName2 = conenctedDeviceName;
						toastConnectedDevice(connectedDeviceName2);
						MultiplayerActivity.this.setProgessValue(2);
						if (bluetoothMPService.getMaxDeviceNumber() != 2) {
							conStatus = new int[]{bluetoothMPService.getMaxDeviceNumber(), 2};
							MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_WAITING,
									conStatus), BluetoothMPService.ALL_DEVICES);
						}
						break;
					case 3:
						connectedDeviceName3 = conenctedDeviceName;
						toastConnectedDevice(connectedDeviceName3);
						MultiplayerActivity.this.setProgessValue(3);
						if (bluetoothMPService.getMaxDeviceNumber() != 3) {
							conStatus = new int[]{bluetoothMPService.getMaxDeviceNumber(), 3};
							MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_WAITING,
									conStatus), BluetoothMPService.ALL_DEVICES);
						}
						break;
					case 4:
						connectedDeviceName4 = conenctedDeviceName;
						toastConnectedDevice(connectedDeviceName4);
						MultiplayerActivity.this.setProgessValue(4);
						if (bluetoothMPService.getMaxDeviceNumber() != 4) {
							conStatus = new int[]{bluetoothMPService.getMaxDeviceNumber(), 4};
							MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_WAITING,
									conStatus), BluetoothMPService.ALL_DEVICES);
						}
						break;
					case 5:
						 connectedDeviceName5 = conenctedDeviceName;
						 toastConnectedDevice(connectedDeviceName5);
						 MultiplayerActivity.this.setProgessValue(5);
						 if (bluetoothMPService.getMaxDeviceNumber() != 5) {
							 conStatus = new int[]{bluetoothMPService.getMaxDeviceNumber(), 5};
							 MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_WAITING,
									conStatus), BluetoothMPService.ALL_DEVICES);
						 }
						 break;
					case 6:
						 connectedDeviceName6 = conenctedDeviceName;
						 toastConnectedDevice(connectedDeviceName6);
						 MultiplayerActivity.this.setProgessValue(6);
						 if (bluetoothMPService.getMaxDeviceNumber() != 6) {
							 conStatus = new int[]{bluetoothMPService.getMaxDeviceNumber(), 6};
							 MultiplayerActivity.this.sendMessage(new DataTransfer(DataTransfer.STATUS_WAITING,
										conStatus), BluetoothMPService.ALL_DEVICES);
						 }
						 break;
					case 7:
						 connectedDeviceName7 = conenctedDeviceName;
						 toastConnectedDevice(connectedDeviceName7);
						 MultiplayerActivity.this.setProgessValue(7);
						 break;

					}

				}

				// Client gets the connected to the server message
				if (currentState == BluetoothMPService.STATE_CONNECTED_TO_SERVER) {
					connectedServerName = msg.getData().getString(DEVICE_NAME);
					// Toast the server's name
					Toast.makeText(getApplicationContext(),
							getString(R.string.connected_to_server) + " " +
							connectedServerName, Toast.LENGTH_SHORT).show();
					// Show the server's name on the title
					titleBar.setText("@" + connectedServerName);
				}
				break;
			case MESSAGE_TOAST:
				// Toast game directives coming from HumanPlayer
				TextView text = (TextView) toastLayout.findViewById(R.id.toast_text);
				text.setText(msg.getData().getString(TOAST));

				Toast toast = new Toast(MultiplayerActivity.this);
				toast.setGravity(Gravity.BOTTOM, 0, 60);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setView(toastLayout);
				toast.show();
				break;
			case MESSAGE_TOAST_WARNING:
				// Toast the warnings about connection status coming from BluetoothMPService
				if (waitingDialog != null) {
					waitingDialog.dismiss();
					waitingDialog = null;
				}
				
				if (waitingTeamSettings != null) {
					waitingTeamSettings.dismiss();
					waitingTeamSettings = null;
				}
				Toast toast_warn = Toast.makeText(MultiplayerActivity.this, msg.getData().getString(TOAST),
						Toast.LENGTH_LONG);
				toast_warn.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast_warn.show();
				openOptionsMenu();
				break;				
			case MESSAGE_TITLE:
				// Change the title text according the message coming from BluetoothMPService
				titleBar.setText(msg.getData().getString(TITLE));
				break;
			}
		}

		/**
		 * Toast the connected client device name
		 * @param connectedDeviceName
		 * 			Name of the client device
		 */
		private void toastConnectedDevice(String connectedDeviceName) {
			Toast.makeText(MultiplayerActivity.this,
					getString(R.string.connected_to) + " " + connectedDeviceName1,
					Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * create menu button's menu to offer the conenction
	 * options during the activity without going back to welcome
	 * screen
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
		if (D) Log.d(TAG, "ensure discoverable");
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
		if (D) Log.d(TAG, "onActivityResult " + resultCode);
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
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				// setup multiplayer mode
				setupMultiPlayer();
			} else {
				// Bluetooth isn't enabled or an error occurred
				if (D) Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.try_singleplayer,
						Toast.LENGTH_LONG).show();
				finish();
			}
		case REQUEST_MODE_TYPE:
			if (resultCode == RESULT_CLIENT_MODE) {
				// Device is client, select server device to connect
				if (D) Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_CLIENT_MODE");
				Intent connectServerIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(connectServerIntent, REQUEST_CONNECT_SERVER);
			} else if (resultCode == RESULT_SERVER_MODE){
				// Device is server
				if (D) Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_SERVER_MODE");
				if (clientNumberPickerIntent == null) {
					// Select the number of clients
					if (D) Log.d(TAG, "clientNumberPickerIntent == null");
					clientNumberPickerIntent = new Intent(this, ClientNumberPicker.class);
					clientNumberPickerIntent.putExtra(MAX_CLIENTS, numberOfClients);
					startActivityForResult(clientNumberPickerIntent, REQUEST_MODE_TYPE);
				} else {
					// set the selected clients amount
					if (D) Log.d(TAG, "clientNumberPickerIntent != null");
					clientNumberPickerIntent = null;
					numberOfClients = data.getExtras().getInt(MAX_CLIENTS);
					bluetoothMPService.setMaxDeviceNumber(numberOfClients);
					if (D) Log.d(TAG, "numberOfClients: " + numberOfClients);
					
					if (bluetoothMPService != null) {
						if (bluetoothMPService.getState() == BluetoothMPService.STATE_NONE) {
							// Start server listening mode
							bluetoothMPService.startServer();
						}
					}
					// Create progress dialog to show the device connection activity
					MultiplayerActivity.this.waitingDialog = new ProgressDialog(MultiplayerActivity.this);
					MultiplayerActivity.this.waitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					MultiplayerActivity.this.waitingDialog.setMessage(getString(R.string.waiting_others));
					MultiplayerActivity.this.waitingDialog.setCancelable(true);
					MultiplayerActivity.this.waitingDialog.setMax(bluetoothMPService.getMaxDeviceNumber());
					MultiplayerActivity.this.waitingDialog.show();
					if (D) Log.d(TAG, "Creating Progress Dialog is successful!");
				}
			} else if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_CANCELED");
				finish();
			} else if (resultCode == RESULT_GOBACK) {
				Log.d(TAG, "REQUEST_MODE_TYPE: RESULT_GOBACK");
				finish();
			}
			break;
		case REQUEST_SET_PLAYERS:
			if (resultCode == RESULT_OK) {

				int teams[] = data.getExtras().getIntArray(TeamMatching.key);
				int start[][] = new int[teams.length / 4][4];
				for (int i = 0; i < teams.length; ++i)
					start[i / 4][i % 4] = teams[i];
				startGame(start[0]);
				for (int i = 0; i < bluetoothMPService.getConnectedDevices(); ++i)
					sendMessage(new DataTransfer(DataTransfer.SETUP_GAME, start[i + 1]), i + 1);
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
	private void sendMessage(Object data, int deviceNo) {
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
			bluetoothMPService.write(send, deviceNo);
		}
	}
	
	/**
	 * Set the value of the progress bar when server device
	 * is waiting for client devices to connect
	 * @param value Number of devices connected
	 */
    private void setProgessValue(int value) {
    	// convert the value to percentage
    	if (!waitingDialog.isShowing()) MultiplayerActivity.this.waitingDialog.show();
		MultiplayerActivity.this.waitingDialog.setProgress(value);
		Log.d(TAG, "setProgress(barValue) SUCCESS!");
        if (value == bluetoothMPService.getMaxDeviceNumber()){
        	// all connected
        	waitingDialog.dismiss();
        }
    }
    
    public static final void notifyPlayers(int[] token) {
		if (activity != null) {
			addToken(token, false);
			activity.sendMessage(
					new DataTransfer(DataTransfer.IS_NOTIFICATION, token),
					activity.bluetoothMPService.serverDevice ? BluetoothMPService.ALL_DEVICES
							: BluetoothMPService.SERVER_ID);
		}
    }
    
    /**
     * Initialize the player on the board and start the first movement 
     */
	private void startGame(int teams[]) {
		Log.d(TAG, "startGame()");
	      players = new Player[Board.getTeams()];
	      int playing = 4;
	      for (int i = 0; i < teams.length; ++i)
	    	  if (teams[i] == TeamMatching.int_disabled) {
	    		  --playing;
	    		  continue;
	    	  }
	    	  else if (teams[i] == TeamMatching.int_human)
	    		  players[i] = new HumanPlayer(Team.getById(i));
	    	  else if (teams[i] >= TeamMatching.offset_strategy)
	    		  players[i] = new AIPlayer(Team.getById(i), teams[i] - TeamMatching.offset_strategy);
	    	  else
	    		  players[i] = new NetworkPlayer(Team.getById(i));
	      Board.startGame(playing);
	}
	
	/**
	 * Convert data arrived from other device to corresponding
	 * object according to the device mode
	 * 
	 * @param readBuf data arrived from other device
	 */
	protected void convertArrivalData(byte[] readBuf, int deviceNo) {
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

		// process the data
		processArrivalData((DataTransfer) o, deviceNo);
	}

	/**
	 * Process data arrived
	 * 
	 * @param transfer
	 */
	private void processArrivalData(DataTransfer transfer,  int deviceNo) {
		Log.d(TAG, "processArrivalServerData() --");
		switch (transfer.reason) {
		case DataTransfer.SETUP_GAME:
			if (MultiplayerActivity.this.waitingDialog != null) {
				waitingDialog.dismiss();
				waitingDialog = null;
			}
			waitingTeamSettings.cancel();
			waitingTeamSettings = null;
			Toast.makeText(getApplicationContext(),
					"start game", Toast.LENGTH_SHORT).show();
			startGame(transfer.tokens);
			break;
		case DataTransfer.IS_NOTIFICATION:
			addToken(transfer.tokens, true);
			--deviceNo;
			if (bluetoothMPService.serverDevice)
				for (int i = 0; i < bluetoothMPService.getConnectedDevices(); ++i)
					if (i != deviceNo) // forward message to clients
						activity.sendMessage(new DataTransfer(
								DataTransfer.IS_NOTIFICATION, transfer.tokens),
								i + 1);
			break;
		case DataTransfer.STATUS_WAITING:
			if (MultiplayerActivity.this.waitingDialog == null) {
				// Create progress dialog to show the device connection activity
				bluetoothMPService.setMaxDeviceNumber(transfer.tokens[0]);
				if (D) Log.d(TAG, "transfer.tokens[1]: " + transfer.tokens[1] + " ,transfer.tokens[0]: " + transfer.tokens[0]);
				MultiplayerActivity.this.waitingDialog = new ProgressDialog(MultiplayerActivity.this);
				MultiplayerActivity.this.waitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				MultiplayerActivity.this.waitingDialog.setMessage(getString(R.string.waiting_others));
				MultiplayerActivity.this.waitingDialog.setCancelable(true);
				MultiplayerActivity.this.waitingDialog.setMax(bluetoothMPService.getMaxDeviceNumber());
				MultiplayerActivity.this.waitingDialog.show();
				if (D) Log.d(TAG, "Creating Progress Dialog is successful!");
				MultiplayerActivity.this.setProgessValue(transfer.tokens[1]);
			} else {
				MultiplayerActivity.this.setProgessValue(transfer.tokens[1]);
			}
			break;
		case DataTransfer.STATUS_SETTINGS:
			if (D) Log.i(TAG, "STATUS_SETTINGS");
			if (MultiplayerActivity.this.waitingDialog != null) {
				waitingDialog.dismiss();
				waitingDialog = null;
			}
			waitingTeamSettings = new ProgressDialog(MultiplayerActivity.this);
			waitingTeamSettings.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			waitingTeamSettings.setMessage(getString(R.string.waiting_team_settings_dialog));
			waitingTeamSettings.setCancelable(true);
			waitingTeamSettings.show();
			if (D) Log.i(TAG, "dialog is created");
			break;
		}
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
    
    private static final void addToken(int[] token, boolean real) {
//    	Log.d("multiplayer", "add token");
    	if (tokens != null)
	    	synchronized (tokens) {
		    	tokens.add(token);
				if (real && tokens.size() == 1)
					NetworkPlayer.notify(tokens.element());
	    	}
    }
}