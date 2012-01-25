package de.tum.multiplayer.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.tum.MenschAergereDichNichtActivity;
import de.tum.multiplayer.MultiplayerActivity;

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

	// Unique UUID
	private static final UUID MY_UUID = UUID
			.fromString("c0a8648d-7382-457a-8de3-6c5aca4d922e");

	// Local bluetooth adapter
	private final BluetoothAdapter bluetoothAdapter;
	// Handler to communicate with MultiplayerActivity
	private final Handler handler;
	// Threads that making communication on server and client side
	private AcceptClientThread acceptThread;
	private ConnectToServerThread connectToServerThread;
	private ConnectedThread connectedClientThread;
	private ConnectedThread connectedThread1;
	private ConnectedThread connectedThread2;
	private ConnectedThread connectedThread3;
	// State of the communication
	private int comState;

	// Communication states
	// no connection
	public static final int STATE_NONE = 0;
	// listening for incoming client connections
	public static final int STATE_LISTEN = 1;
	// connecting to the server
	public static final int STATE_CONNECTING_TO_SERVER = 2;
	// now first device is connected to server
	public static final int STATE_CONNECTED_1 = 3;
	// now second device is connected to server
	public static final int STATE_CONNECTED_2 = 4;
	// now third device is connected to server
	public static final int STATE_CONNECTED_3 = 5;
	// all the devices connected to the server
	public static final int STATE_ALL_CONNECTED = 6;
	// client connected to the server
	public static final int STATE_CONNECTED_TO_SERVER = 7;

	// number of current connected devices to server
	private int connectedDevices = 0;
	// if current device is server/client
	public boolean serverDevice = false;
	// max number of devices allowed to connect
	private int maxDeviceNumber = 1;
	// server device id
	private static final int SERVER_ID = -1;

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
		handler.obtainMessage(MultiplayerActivity.MESSAGE_STATE_CHANGE, state,
				-1).sendToTarget();
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

		// Cancel earlier attempts to connect another server
//		if (connectToServerThread != null) {
//			connectToServerThread.cancel();
//			connectToServerThread = null;
//		}

		// Start the listening for client connections
		if (acceptThread == null) {
			acceptThread = new AcceptClientThread();
			acceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * Thread that listening for client connections
	 */
	private class AcceptClientThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;

		public AcceptClientThread() {
			BluetoothServerSocket tmp = null;

			// Create a new listening server socket
			try {
				tmp = bluetoothAdapter
						.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "BEGIN AcceptClientThread" + this);
			setName("AcceptClientThread");
			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (true) {
				try {
					// blocking call
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "Server's accept() failed", e);
					break;
				}

				// connection is accepted
				if (socket != null) {
					if (D) Log.d(TAG, "AcceptClientThread, socket != null");
					connected(socket, socket.getRemoteDevice());
				}
			}
		}

		public void cancel() {
			if (D) Log.d(TAG, "cancel " + this);
			try {
				mmServerSocket.close();
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
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D) Log.d(TAG, "Server is connected to client");

		// Cancel the thread for client connection
		if (connectToServerThread != null) {
			connectToServerThread.cancel();
			connectToServerThread = null;
		}

		// set the state according the number of devices connected
		if (getConnectedDevices() < maxDeviceNumber) {
			Log.d(TAG, "connectedDevices++");
			setConnectedDevices(getConnectedDevices() + 1); // TODO no need??
			if (getConnectedDevices() == 1) {
				serverDevice = true;
				setState(STATE_CONNECTED_1);
			}
			if (getConnectedDevices() == 2) setState(STATE_CONNECTED_2);
			if (getConnectedDevices() == 3) setState(STATE_CONNECTED_3);
		}

		// start ConnectedThread to initiate data transfer mechanism
		if (comState == STATE_CONNECTED_1) {
			connectedThread1 = new ConnectedThread(socket,
					getConnectedDevices());
			connectedThread1.start();

			Message msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			Bundle bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);

		} else if (comState == STATE_CONNECTED_2) {
			connectedThread2 = new ConnectedThread(socket,
					getConnectedDevices());
			connectedThread2.start();

			Message msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			Bundle bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
		} else if (comState == STATE_CONNECTED_3) {
			connectedThread3 = new ConnectedThread(socket,
					getConnectedDevices());
			connectedThread3.start();

			Message msg = handler
					.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
			Bundle bundle = new Bundle();
			bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
			msg.setData(bundle);
			handler.sendMessage(msg);
		}

		// Cancel the accept thread for more than MAX_DEVICE
		if (getConnectedDevices() == maxDeviceNumber) {
			if (D) Log.d(TAG, "connectedDevices == maxDeviceNumber");
			if (acceptThread != null) {
				acceptThread.cancel();
				acceptThread = null;
				setState(STATE_ALL_CONNECTED);
				Log.d(TAG, "mAcceptThread.cancel()");
			}
		}
	}

	/**
	 * Clients connects to Server
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

		// Cancel all the threads that running a connection
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
		if (connectedClientThread != null) {
			connectedClientThread.cancel();
			connectedClientThread = null;
		}

		// This is client device so cancel the AcceptThread
		if (acceptThread != null) {
			acceptThread.cancel();
			acceptThread = null;
		}

		// Connect to the server
		connectToServerThread = new ConnectToServerThread(device);
		connectToServerThread.start();
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
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "client couldn't get socket", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			if(D) Log.d(TAG, "BEGIN ConnectToServerThread");
			setName("ConnectThread");

			// Cancel discovery in case it's still running
			bluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// blocking call
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				Log.e(TAG, "Client couldn't connect to server via mmSocket.connect()");
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,"unable to close() socket during connection failure", e2);
				}
				Log.e(TAG, "Exception occurred at ConnectToServerThread");
				setState(STATE_NONE);
				return;
			}

			// Reset the ConnectToServerThread because we're done
			synchronized (BluetoothMPService.this) {
				connectToServerThread = null;
			}

			// Start the connected thread
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
	 * Client connected to server, now handle the data transfer
	 * 
	 * @param socket
	 *            socket that made connection to server
	 * @param device
	 *            server
	 */
	public synchronized void connectedClient(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "Client connected");

		// Client connection is done, so cancel its thread
		if (connectToServerThread != null) {
			connectToServerThread.cancel();
			connectToServerThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		connectedClientThread = new ConnectedThread(socket, SERVER_ID); // TODO
		connectedClientThread.start();
		
		serverDevice = false;
		setState(STATE_CONNECTED_TO_SERVER);

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
		private final int DeviceNo;

		public ConnectedThread(BluetoothSocket socket, int DeviceNo) {
			Log.d(TAG, "create ConnectedThread for Client or Server");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			this.DeviceNo = DeviceNo; // SERVER_ID is the server

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
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
							bytes, DeviceNo, buffer).sendToTarget();

				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
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

				// TODO no need!
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
	public void write(byte[] out) {

		if (serverDevice) {
			// Create temporary objects
			ConnectedThread r1 = null;
			ConnectedThread r2 = null;
			ConnectedThread r3 = null;

			// Synchronize copies of the ConnectedThreads
			synchronized (this) {
				if (comState != STATE_ALL_CONNECTED)
					return;
				r1 = connectedThread1;
				r2 = connectedThread2;
				r3 = connectedThread3;
			}
			// Perform the write unsynchronized
			if (r1 != null)
				r1.write(out);
			if (r2 != null)
				r2.write(out);
			if (r3 != null)
				r3.write(out);
		} else {
			ConnectedThread client = null;

			synchronized (this) {
				if (comState != STATE_CONNECTED_TO_SERVER)
					return;
				client = connectedClientThread;
			}
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
				.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MenschAergereDichNichtActivity.TOAST,
				"Couldn't connect! Use menu button to continue");
		msg.setData(bundle);
		handler.sendMessage(msg);
		// Make it possible to be server again TODO
		BluetoothMPService.this.startServer();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		setState(STATE_LISTEN); // TODO

		// Send a failure message back to the Activity
		Message msg = handler
				.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MenschAergereDichNichtActivity.TOAST,
				"Device connection was lost");
		msg.setData(bundle);
		handler.sendMessage(msg);
		stop(); // TODO
		// Make it possible to be server again
		// BluetoothMPService.this.startServer();
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		if (connectToServerThread != null) {
			connectToServerThread.cancel();
			connectToServerThread = null;
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
		if (connectedClientThread != null) {
			connectedClientThread.cancel();
			connectedClientThread = null;
		}
		if (acceptThread != null) {
			acceptThread.cancel();
			acceptThread = null;
		}
		setConnectedDevices(0);
		serverDevice = false;
		if (comState != STATE_NONE)
			setState(STATE_NONE);
	}

	public int getMaxDeviceNumber() {
		return maxDeviceNumber;
	}

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
