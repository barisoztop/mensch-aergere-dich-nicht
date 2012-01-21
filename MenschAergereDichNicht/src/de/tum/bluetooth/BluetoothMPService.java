
package de.tum.bluetooth;

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
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothMPService {
    // Debugging
    private static final String TAG = "BluetoothMPService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "MenschAergereDichNicht";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("c0a8648d-7382-457a-8de3-6c5aca4d922e");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptClientThread mAcceptThread;
    private ConnectToServerThread mConnectThread;
    private ConnectedThread mConnectedClientThread;
    private ConnectedThread mConnectedThread1;
    private ConnectedThread mConnectedThread2;
    private ConnectedThread mConnectedThread3;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING_TO_SERVER = 2; 		// connecting to the server device
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_ALL_CONNECTED = 4;	// all the devices connected to the server
	public static final int STATE_CONNECTED_TO_SERVER = 5;
	
    public int connectedDevices = 0;
    public boolean serverDevice = false;
    private int maxDeviceNumber = 1;
	private static final int SERVER_ID = -1;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothMPService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MultiplayerActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void startServer() {
        if (D) Log.d(TAG, "start Service (again)");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptClientThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Clients connects to Server
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connectServer(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to server: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING_TO_SERVER) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread1 != null) {mConnectedThread1.cancel(); mConnectedThread1 = null;}
        if (mConnectedThread2 != null) {mConnectedThread2.cancel(); mConnectedThread2 = null;}
        if (mConnectedThread3 != null) {mConnectedThread3.cancel(); mConnectedThread3 = null;}
        if (mConnectedClientThread != null) {mConnectedClientThread.cancel(); mConnectedClientThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectToServerThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING_TO_SERVER);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connectedClient(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "Client connected");

        // Cancel the thread that doing client connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread1 != null) {mConnectedThread1.cancel(); mConnectedThread1 = null;}
        if (mConnectedThread2 != null) {mConnectedThread2.cancel(); mConnectedThread2 = null;}
        if (mConnectedThread3 != null) {mConnectedThread3.cancel(); mConnectedThread3 = null;}
        if (mConnectedClientThread != null) {mConnectedClientThread.cancel(); mConnectedClientThread = null;}

        // This is client device so cancel the AcceptThread
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedClientThread = new ConnectedThread(socket, SERVER_ID); // TODO -1 is the server that we connected, socket also server's socket
        mConnectedClientThread.start();

        // Send the name of the connected Server back to the UI Activity
        Message msg = mHandler.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        serverDevice = false;
        setState(STATE_CONNECTED_TO_SERVER);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "Server is connected to client");

        // Cancel the thread for client connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
//        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        
        // TODO restart AcceptThread
        if (D) Log.d(TAG, "Cancel mAcceptThread and Restart it with BluetoothMPService.this.start()");

        
        if (connectedDevices < maxDeviceNumber)  {
        	Log.d(TAG,"connectedDevices++");
        	connectedDevices++;
        }
        setState(STATE_CONNECTED); // TODO state for each device
        
        serverDevice = true; // TODO put it under connectedDevices == 1
        
        // Start the thread to manage the connection and perform transmissions
        if (connectedDevices == 1) {        	
        	mConnectedThread1 = new ConnectedThread(socket, connectedDevices);
            mConnectedThread1.start();
            
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            
        } else if (connectedDevices == 2) {
        	mConnectedThread2 = new ConnectedThread(socket, connectedDevices);
            mConnectedThread2.start();
            
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else if (connectedDevices == 3){
        	mConnectedThread3 = new ConnectedThread(socket, connectedDevices);
            mConnectedThread3.start();
            
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(MultiplayerActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MultiplayerActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        
        // Cancel the accept thread for more than MAX_DEVICE
        if (connectedDevices == maxDeviceNumber){
        	if (D) Log.d(TAG, "connectedDevices == maxDeviceNumber so mAcceptThread.cancel()");
        	if (mAcceptThread != null) {
        		mAcceptThread.cancel();
        		mAcceptThread = null;
        		setState(STATE_ALL_CONNECTED);
        		Log.d(TAG, "mAcceptThread.cancel()");
        	}
        } else {
            if (D) Log.d(TAG, "Restart mAcceptThread with BluetoothMPService.this.start()");
//            mAcceptThread.cancel();
//            mAcceptThread = null;
//            BluetoothMPService.this.startServer();
            setState(STATE_LISTEN);
        }
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread1 != null) {mConnectedThread1.cancel(); mConnectedThread1 = null;}
        if (mConnectedThread2 != null) {mConnectedThread2.cancel(); mConnectedThread2 = null;}
        if (mConnectedThread3 != null) {mConnectedThread3.cancel(); mConnectedThread3 = null;}
        if (mConnectedClientThread != null) {mConnectedClientThread.cancel(); mConnectedClientThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        connectedDevices = 0;
        serverDevice = false;
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {        

        
        // Synchronize a copy of the ConnectedThread
        if (serverDevice) {
        	// Create temporary object
            ConnectedThread r1 = null;
            ConnectedThread r2 = null;
            ConnectedThread r3 = null;
            
	        synchronized (this) {
	            if (mState != STATE_ALL_CONNECTED) return;
	            r1 = mConnectedThread1;
	            r2 = mConnectedThread2;
	            r3 = mConnectedThread3;  
	        }
	        // Perform the write unsynchronized
	        // TODO if are debugging
        	if (r1 != null) r1.write(out);
        	if (r2 != null) r2.write(out);
        	if (r3 != null) r3.write(out);
        } else {
        	
        	ConnectedThread client = null;
        	
            synchronized (this) {
                if (mState != STATE_CONNECTED_TO_SERVER) return;
                client = mConnectedClientThread;
            }
            // Perform the write unsynchronized
            client.write(out);
        }
        	
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MenschAergereDichNichtActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Make it possible to be server again
        BluetoothMPService.this.startServer();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MenschAergereDichNichtActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        stop();
        // Make it possible to be server again
        BluetoothMPService.this.startServer();
    }

    public int getMaxDeviceNumber() {
		return maxDeviceNumber;
	}

	public void setMaxDeviceNumber(int maxDeviceNumber) {
		this.maxDeviceNumber = maxDeviceNumber;
	}

	/**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptClientThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptClientThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN AcceptClientThread" + this);
            setName("AcceptClientThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Server's accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                	Log.i(TAG, "A connection is accepted by client, socket != null");
                	connected(socket, socket.getRemoteDevice());
                }
            }
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectToServerThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectToServerThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "client couldn't get socket", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectToServerThread");
            setName("ConnectThread");

            // Cancel discovery in case it's still running
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                Log.d(TAG, "Client couldn't connect to server via mmSocket.connect()");
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Exception occurred so start the service again
                Log.e(TAG,"Exception occurred at ConnectToServerThread");
                setState(STATE_NONE);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothMPService.this) {
                mConnectThread = null;
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
     * This thread runs during a connection with a remote device either for server or client
     * It handles all incoming and outgoing transmissions.
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
                    // Read from the InputStream for Server's messages
                    bytes = mmInStream.read(buffer);
//                    if (serverDevice) write(buffer); // TODO don't send to your self

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MultiplayerActivity.MESSAGE_READ, bytes, DeviceNo, buffer).sendToTarget();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MultiplayerActivity.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
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
}
