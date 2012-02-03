package de.tum.multiplayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import de.tum.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Consists threads to handle the communication with other devices
 */
public class BluetoothMPService {
	// Debugging
	private static final String TAG = "BluetoothMPService";
	private static final boolean D = true;

	// Name for the SDP record when creating server socket
	private static final String NAME = "MenschAergereDichNicht";

	// Unique UUIDs for each device
	private ArrayList<UUID> uuidList;
	private static final UUID UUID1 = UUID.fromString("7ab4f8ae-289f-4a2f-8474-ba12a58ec499");
	private static final UUID UUID2 = UUID.fromString("142c58fe-8432-4931-95b3-febd4afab6df");
	private static final UUID UUID3 = UUID.fromString("09d183b6-2b92-4aae-85e2-4034eb91d92c");
	private static final UUID UUID4 = UUID.fromString("604fefe4-ef42-4eb9-9ec3-7d9fa8129441");
	private static final UUID UUID5 = UUID.fromString("699313fc-4edc-48ac-81c3-8be64fd17f1c");
	private static final UUID UUID6 = UUID.fromString("923abba7-63a4-42e0-977a-474e81e670fe");
	private static final UUID UUID7 = UUID.fromString("f0b3ef4a-68cd-437a-ab91-1db999a4b2e1");

	// Local bluetooth adapter
	private final BluetoothAdapter bluetoothAdapter;
	// Handler to communicate with MultiplayerActivity
	private final Handler handler;
	// Threads that handling communication on server and client side
	private AcceptClientThread acceptClientThread;
	private ConnectToServerThread connectToServerThread;
	private ConnectedThread connectedClientThread;
	private ConnectedThread connectedThread1;
	private ConnectedThread connectedThread2;
	private ConnectedThread connectedThread3;
	private ConnectedThread connectedThread4;
	private ConnectedThread connectedThread5;
	private ConnectedThread connectedThread6;
	private ConnectedThread connectedThread7;
	// State of the communication
	private int comState;

	/* Communication states */
	// no connection
	public static final int STATE_NONE = 0;
	// listening for incoming client connections
	public static final int STATE_LISTEN = 1;
	// connecting to the server
	public static final int STATE_CONNECTING_TO_SERVER = 2;
	// waiting for more clients
	public static final int STATE_WAITING_FOR_CONNECTIONS = 3;
	// all devices are connected
	public static final int STATE_ALL_CONNECTED = 4;
	// client connected to the server
	public static final int STATE_CONNECTED_TO_SERVER = 5;

	// MultiplayerActivity's context
	Context context;
	// number of current connected devices to server
	private int connectedDevices = 0;
	// if current device is server/client
	public boolean serverDevice = false;
	// max number of devices allowed to connect
	private int maxDeviceNumber = 1;
	// server device id
	public static final int SERVER_ID = -1;
	// Request key for all of the devices
	public static final int ALL_DEVICES = -2;
	// Probing incremental for UUID server/client
	private int uuidTrying = 0;

	/**
	 * Constructor to get the handler from MultiplayerActivity,and its context
	 * 
	 * @param context
	 *            MultiplayerActivity's context
	 * @param handler
	 *            Handler for the communication with MultiplayerActivity
	 */
	public BluetoothMPService(Context context, Handler handler) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		comState = STATE_NONE;
		this.handler = handler;
		this.context = context;
		uuidList = new ArrayList<UUID>();
		uuidList.add(UUID1);
		uuidList.add(UUID2);
		uuidList.add(UUID3);
		uuidList.add(UUID4);
		uuidList.add(UUID5);
		uuidList.add(UUID6);
		uuidList.add(UUID7);
	}

	/**
	 * Update the state of the communication
	 * 
	 * @param state
	 *            State constant
	 */
	private synchronized void setState(int state) {
		if (D) Log.d(TAG, "setState(): " + comState + " to " + state);
		comState = state;

		// let the MultiplayerActivity know
		handler.obtainMessage(MultiplayerActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current communication state.
	 */
	public synchronized int getState() {
		return comState;
	}

	/**
	 * Start the server mode by starting the AcceptClientThread
	 */
	public synchronized void startServer() {
		if (D) Log.d(TAG, "startServer()");

		// Start the listening for client connections
		if (acceptClientThread == null) {
			acceptClientThread = new AcceptClientThread();
		}
	}

	/**
	 * Thread that listening for client connections
	 */
	private class AcceptClientThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket sBluetoothServerSocket;

		public AcceptClientThread() {
			setName("AcceptClientThread" + uuidTrying);
			BluetoothServerSocket tmp = null;

			if (D) Log.d(TAG, "Server use this UUID: " + uuidList.get(uuidTrying));
			// Create a new listening server socket
			try {
				tmp = bluetoothAdapter
						.listenUsingRfcommWithServiceRecord(NAME, uuidList.get(uuidTrying));
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			sBluetoothServerSocket = tmp;
			start();
		}

		public void run() {
			if (D) Log.d(TAG, "BEGIN AcceptClientThread" + this + " UUID no: " + uuidList.get(uuidTrying));
			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (true) {
				try {
					// blocking call
					socket = sBluetoothServerSocket.accept();
				} catch (IOException e) {
					Log.i(TAG, "Server's accept() stopped/failed: connectedDevices: " + connectedDevices);
					break;
				}

				// connection is accepted
				if (socket != null) {
					if (D) Log.d(TAG, "AcceptClientThread: Socket is accepted by client");
					cancel();
					connected(socket, socket.getRemoteDevice());
				}
			}
		}

		public void cancel() {
			if (D) Log.d(TAG, "Cancel AcceptClientThread named: " + this);
			try {
				sBluetoothServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed");
			}
		}
	}

	/*
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket The BluetoothSocket on which the connection was made
	 * @param device The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D) Log.d(TAG, "Server is connected to client");

		// Cancel if this was client before
		if (connectToServerThread != null) {
			connectToServerThread.cancel();
			connectToServerThread = null;
		}
		
		// set the state according the number of devices connected
		if (connectedDevices <= maxDeviceNumber) {
			connectedDevices++;
			uuidTrying++; // Prepare for next clients
			if (D) Log.d(TAG, "Server: connectedDevices: " + connectedDevices + "/" + maxDeviceNumber 
					+ " ,UUID no:" + uuidTrying);
			if (connectedDevices == 1) serverDevice = true;
			setState(STATE_WAITING_FOR_CONNECTIONS);
		}

		Message msg = null;
		Bundle bundle = null;
		// start ConnectedThread to initiate data transfer mechanism
		switch (connectedDevices) {
		case 1:
			connectedThread1 = new ConnectedThread(socket, connectedDevices, device);
			// Let the MultiplayerActivity know about the device name
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		case 2:
			connectedThread2 = new ConnectedThread(socket, connectedDevices, device);
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		case 3:
			connectedThread3 = new ConnectedThread(socket, connectedDevices, device);
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		case 4:
			connectedThread4 = new ConnectedThread(socket, connectedDevices, device);
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		case 5:
			connectedThread5 = new ConnectedThread(socket, connectedDevices, device);
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		case 6:
			connectedThread6 = new ConnectedThread(socket, connectedDevices, device);
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		case 7:
			connectedThread7 = new ConnectedThread(socket, connectedDevices, device);
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
			break;
		}

		if (connectedDevices == maxDeviceNumber) {
			// Cancel the accept thread for more than maxDeviceNumber
			if (D) Log.d(TAG, "connectedDevices == maxDeviceNumber");
			if (acceptClientThread != null) {
				acceptClientThread.cancel();
				acceptClientThread = null;
				setState(STATE_ALL_CONNECTED);
			}
		} else {
			// Restart the accept thread for more connections
			if (D) Log.d(TAG, "connectedDevices != maxDeviceNumber, restart AcceptThread");
			if (acceptClientThread != null) {
//				acceptClientThread.cancel();
				acceptClientThread = null;
				BluetoothMPService.this.startServer();
			}
		}
	}

	/**
	 * Client connects to Server
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connectServer(BluetoothDevice device) {
		if (D) Log.d(TAG, "connect to server: " + device);

		// Cancel earlier attempts to connect to server
		if (comState == STATE_CONNECTING_TO_SERVER) {
			if (connectToServerThread != null) {
				connectToServerThread.cancel();
				connectToServerThread = null;
			}
		}

		// kill the remaining thread and set the default values
		stop(false);

		// Connect to the server
		connectToServerThread = new ConnectToServerThread(device);
		setState(STATE_CONNECTING_TO_SERVER);
	}

	/**
	 * Attempt to connect to server device
	 */
	private class ConnectToServerThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectToServerThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for the connection with server
			try {
				tmp = device.createRfcommSocketToServiceRecord(uuidList.get(uuidTrying));
				if(D) Log.d(TAG, "ConnectToServerThread UUID_ID: " + uuidTrying + " UUID: " + uuidList.get(uuidTrying));
			} catch (IOException e) {
				Log.e(TAG, "Client couldn't get socket", e);
			}
			mmSocket = tmp;
			start();
		}

		public void run() {
			if(D) Log.d(TAG, "BEGIN ConnectToServerThread");
			setName("ConnectThread" + uuidTrying);

			// Cancel discovery if still running
			if(bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// blocking call
				mmSocket.connect();
			} catch (IOException e) {
				if (D) Log.e(TAG, "Client couldn't connect to server via mmSocket.connect()");
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,"unable to close() socket during connection failure", e2);
				}
				if (D) Log.d(TAG, "clients try next UUID");
				if (uuidTrying < 6) {
					uuidTrying++;
					tryNextUUID(mmDevice);
				} else {
					if (D) Log.d(TAG, "None of the UUID is matched. connectionFailed()");
					connectionFailed();
				}
				return;
			}

			// Reset the ConnectToServerThread because we're done
			synchronized (BluetoothMPService.this) {
				connectToServerThread = null;
			}

			// Start the connected thread
			if(D) Log.d(TAG, "Client starts Connected thread...");
			connectedClient(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
	
	/**
	 * Clients probe next UUID to connect to server
	 * @param device
	 * 			Server
	 */
	public void tryNextUUID (BluetoothDevice device) {
		Log.e(TAG, "Trying next UUID, Try no: " + uuidTrying);
		// Connect to the server
		if (connectToServerThread != null) {
			connectToServerThread.cancel();
			connectToServerThread = null;
		}
		connectToServerThread = new ConnectToServerThread(device);
	}

	/**
	 * Client connected to server, now handle the data transfer
	 * 
	 * @param socket
	 *            socket that made connection to server
	 * @param device
	 *            server
	 */
	public synchronized void connectedClient(BluetoothSocket socket, BluetoothDevice device) {
		if (D) Log.d(TAG, "Client connected");

		// Client connection is done, so cancel its thread
		if (connectToServerThread != null) {
			connectToServerThread.cancel();
			connectToServerThread = null;
		}

		// Start the thread to manage the connection and transmissions
		connectedClientThread = new ConnectedThread(socket, SERVER_ID, device);
		
		serverDevice = false;
		setState(STATE_CONNECTED_TO_SERVER);

		// Let the MultiplayerActivity know about the server device name
		Message msg = handler
				.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	/**
	 * This thread runs during a connection with a remote device either for
	 * server or client. It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private final int deviceNo;
		private final BluetoothDevice mmDevice;

		public ConnectedThread(BluetoothSocket socket, int DeviceNo, BluetoothDevice device) {
			setName("ConnectedThread" + DeviceNo);
			if (D) Log.d(TAG, "Create ConnectedThread for Client or Server, Thread: " + this);
			mmSocket = socket;
			mmDevice = device;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			this.deviceNo = DeviceNo; // SERVER_ID is the server

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			
			start();
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream for messages
					bytes = mmInStream.read(buffer);

					// Send the bytes to MultiplayerActivity
					handler.obtainMessage(MultiplayerActivity.MESSAGE_READ,
							bytes, deviceNo, buffer).sendToTarget();

				} catch (IOException e) {
					Log.e(TAG, "mConnectedThread disconnected");
					connectionLost(mmDevice.getName());
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);

				// Share the sent message back to the MultiplayerActivity
				handler.obtainMessage(MultiplayerActivity.MESSAGE_WRITE, -1,
						-1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out, int deviceNo) {

		if (serverDevice) {
			// Create temporary objects
			ConnectedThread r1 = null;
			ConnectedThread r2 = null;
			ConnectedThread r3 = null;
			ConnectedThread r4 = null;
			ConnectedThread r5 = null;
			ConnectedThread r6 = null;
			ConnectedThread r7 = null;

			// Synchronize copies of the ConnectedThreads
			synchronized (this) {
				r1 = connectedThread1;
				r2 = connectedThread2;
				r3 = connectedThread3;
				r4 = connectedThread4;
				r5 = connectedThread5;
				r6 = connectedThread6;
				r7 = connectedThread7;
			}
			// Perform the write unsynchronized
			if (r1 != null && (deviceNo == 1 || deviceNo == ALL_DEVICES))
				r1.write(out);
			if (r2 != null && (deviceNo == 2 || deviceNo == ALL_DEVICES))
				r2.write(out);
			if (r3 != null && (deviceNo == 3 || deviceNo == ALL_DEVICES))
				r3.write(out);
			if (r4 != null && (deviceNo == 4 || deviceNo == ALL_DEVICES))
				r4.write(out);
			if (r5 != null && (deviceNo == 5 || deviceNo == ALL_DEVICES))
				r5.write(out);
			if (r6 != null && (deviceNo == 6 || deviceNo == ALL_DEVICES))
				r6.write(out);
			if (r7 != null && (deviceNo == 7 || deviceNo == ALL_DEVICES))
				r7.write(out);
		} else {
			ConnectedThread client = null;

			synchronized (this) {
				if (comState != STATE_CONNECTED_TO_SERVER)
					return;
				client = connectedClientThread;
			}
			if (client != null && deviceNo == SERVER_ID)
				client.write(out);
		}
	}

	/**
	 * connection attempt failed
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = handler
				.obtainMessage(MultiplayerActivity.MESSAGE_TOAST_WARNING);
		Bundle bundle = new Bundle();
		bundle.putString(MultiplayerActivity.TOAST,
				context.getResources().getString(R.string.connection_failed));
		msg.setData(bundle);
		handler.sendMessage(msg);
		
		setTitleNone();
		// reset all the threads and values
		stop(true);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 * @param deviceName 
	 */
	private void connectionLost(String deviceName) {
		if (D) Log.d(TAG, "connectionLost()");
		// Send a failure message back to the Activity
		Message msg = handler
				.obtainMessage(MultiplayerActivity.MESSAGE_TOAST_WARNING);
		Bundle bundle = new Bundle();
		bundle.putString(MultiplayerActivity.TOAST,
				context.getResources().getString(R.string.connection_lost_part1)
				+ deviceName + context.getResources().getString(R.string.connection_lost_part2));
		msg.setData(bundle);
		handler.sendMessage(msg);
		
		if(serverDevice) {
			msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_TITLE);
			bundle = new Bundle();
			bundle.putString(MultiplayerActivity.TITLE,
					context.getResources().getString(R.string.title_not_all_connected));
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
		else
			setTitleNone();
		
		// reset all the threads and values
		stop(true);
	}

	/**
	 * Set the title about the connection status to "none"
	 */
	private void setTitleNone() {
		Message msg = handler
				.obtainMessage(MultiplayerActivity.MESSAGE_TITLE);
		Bundle bundle = new Bundle();
		bundle.putString(MultiplayerActivity.TITLE,
				context.getResources().getString(R.string.title_none));
		msg.setData(bundle);
		handler.sendMessage(msg);
		
	}


	/**
	 * Stop all threads and set the variables to their
	 * default values
	 * @param startup
	 * 			true if initial run, false if client connected to server
	 */
	public synchronized void stop(boolean startup) {
		if (D) Log.d(TAG, "stop : startup: " + startup);
		
		if (startup)
			if (connectToServerThread != null) {
				connectToServerThread.cancel();
				connectToServerThread = null;
			}
		
		if (acceptClientThread != null) {
			acceptClientThread.cancel();
			acceptClientThread = null;
		}
		if (connectedThread1 != null) {
			connectedThread1.cancel();
			connectedThread1 = null;
		}
		if (connectedThread2 != null) {
			connectedThread2.cancel();
			connectedThread2 = null;
		}
		if (connectedThread3 != null) {
			connectedThread3.cancel();
			connectedThread3 = null;
		}
		if (connectedThread4 != null) {
			connectedThread4.cancel();
			connectedThread4 = null;
		}
		if (connectedThread5 != null) {
			connectedThread5.cancel();
			connectedThread5 = null;
		}
		if (connectedThread6 != null) {
			connectedThread6.cancel();
			connectedThread6 = null;
		}
		if (connectedThread7 != null) {
			connectedThread7.cancel();
			connectedThread7 = null;
		}
		if (startup)
			if (connectedClientThread != null) {
				connectedClientThread.cancel();
				connectedClientThread = null;
			}

		// reset values
		connectedDevices = 0;
		uuidTrying = 0;
		maxDeviceNumber = 1;
		serverDevice = false;
		
		if (startup)
			if (comState != STATE_NONE)
				setState(STATE_NONE);
	}

	/**
	 *
	 * @return
	 * 		max number of allowed devices to connect
	 *		to connect to server
	 */
	public int getMaxDeviceNumber() {
		return maxDeviceNumber;
	}

	/**
	 * Sets the max number of allowed devices to connect
	 * to server
	 * @param maxDeviceNumber
	 */
	public void setMaxDeviceNumber(int maxDeviceNumber) {
		this.maxDeviceNumber = maxDeviceNumber;
	}

	/**
	 * @return the connectedDevices
	 */
	public int getConnectedDevices() {
		return connectedDevices;
	}

	/**
	 * @param connectedDevices
	 *            the connectedDevices to set
	 */
	public void setConnectedDevices(int connectedDevices) {
		this.connectedDevices = connectedDevices;
	}
}
