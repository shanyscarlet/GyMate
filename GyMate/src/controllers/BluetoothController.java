package controllers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Scanner;

import utils.BluetoothUtils.eBluetoothStatus;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class BluetoothController {
	
	private BluetoothDevice _btDevice;
	private BluetoothSocket _socket;
	private BluetoothAdapter _btAdapter;
	private Context _ctx;
	private Handler _handler;
	private ConnectThread _btThread;
	private eBluetoothStatus _btStatus;
	
	
	public eBluetoothStatus getBTStatus() {
		return _btStatus;
	}
	
	public BluetoothDevice get_btDevice() {
		return _btDevice;
	}

	public BluetoothSocket get_socket() {
		return _socket;
	}

	public BluetoothAdapter get_btAdapter() {
		return _btAdapter;
	}

	public BluetoothController(Context i_Ctx) {
		_ctx = i_Ctx;
	}

	public void connectToBTDevice(String i_MacAddr) {
		new ConnectThread(i_MacAddr).start();
	}
	
	private class ConnectThread extends Thread {
		private String _macAddr;
		
		public ConnectThread(String macAddr) {
			_macAddr = macAddr;
			_handler = new Handler();
		}
	    
		@Override
	    public void run() {
	    		  
	    	connectToBTDevice(_macAddr);
	    	
	        // Cancel discovery because it will slow down the connection
	        _btAdapter.cancelDiscovery();
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            _socket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                _socket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        getDataFromBluetoothDevice();
	    }
	    
	    public void connectToBTDevice(String i_MacAddr) {
			BluetoothSocket tmpSocket = null;
			// i_MacAddr = "00:1B:B1:FC:D3:70"; // gils pc mac
			try {
				_btDevice = _btAdapter.getRemoteDevice(i_MacAddr);
				Method m = _btDevice.getClass().getMethod("createRfcommSocket",	new Class[] { int.class });
				tmpSocket = (BluetoothSocket) m.invoke(_btDevice,	Integer.valueOf(1));
			} catch (Exception ex) {
				ex.toString();
			}
			_socket = tmpSocket;
		}
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    @SuppressWarnings("unused")
		public void cancel() {
	        try {
	            _socket.close();
	        } catch (IOException e) { }
	    }
	}

	
	public eBluetoothStatus initBT() {
		
		eBluetoothStatus resultBTStatus = eBluetoothStatus.BT_NOT_INITIALIZED;
		_btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (_btAdapter == null) {
			resultBTStatus = eBluetoothStatus.BT_NOT_SUPPORTED;
		} else if (!_btAdapter.isEnabled()) {
			resultBTStatus = eBluetoothStatus.BT_DISABLED;
		}
		
		_btStatus = resultBTStatus;
		return resultBTStatus;
	}
	
	
	static String convertStreamToString(InputStream i_InputStream) {
	    Scanner s = new Scanner(i_InputStream).useDelimiter("\n");
	    return s.hasNext() ? s.next() : null;
	}

	
	public void getDataFromBluetoothDevice() {
		String message = "";
		try {
			InputStream instream = _socket.getInputStream();
			while (true) {
				message = "";
				message = convertStreamToString(instream);
				final String fMessage = message; // Remove
				if(fMessage != null){
					_handler.post(new Runnable() {
					
						@Override
						public void run() {
							Toast.makeText(_ctx, "Number of returns: " + fMessage, Toast.LENGTH_SHORT).show();
							
						}
					});
				}
			}
		} 
		catch (IOException e) {
		}
	}
}