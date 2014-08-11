package com.example.test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import com.example.test.R.string;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
    public static final String MIME_TEXT_PLAIN = "text/plain";
    //public static final String TAG = "NfcDemo";

	private NfcAdapter _nfcAdapter;
	private TextView _nfcDataTextView;
	private BluetoothDevice _btDevice;
	private BluetoothSocket _socket;
	private BluetoothAdapter _btAdapter;
	private UUID _myUuid = UUID.randomUUID();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		_nfcDataTextView = (TextView)findViewById(R.id.nfcDataText);
		
		initBT();
		initNFC();
		
        handleIntent(getIntent());
		
	}


    private void initNFC() {
    	_nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    	if(_nfcAdapter == null){
			Toast.makeText(getApplicationContext(), "No NFC detected", 1).show();
			finish();
		}
		else if (!_nfcAdapter.isEnabled()) {
				Toast.makeText(getApplicationContext(), "Please activate NFC and press Back to return to the -application!", Toast.LENGTH_LONG).show();
		        startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
        }
		else {
        	_nfcDataTextView.setText(string.nfc_ready);
        }
		
	}


	private void initBT() {
		_btAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(_btAdapter == null)
		{
			Toast.makeText(getApplicationContext(), "No Bluetooth detected", 1).show();
			finish();
		}
		else if(!_btAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, 1);
		}
	}


	@Override
    protected void onResume() {
        super.onResume();
         
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown. 
         */
        //setupForegroundDispatch(this, _nfcAdapter);
    }
     
    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        //stopForegroundDispatch(this, _nfcAdapter);
         
        super.onPause();
    }
     
    @Override
    protected void onNewIntent(Intent i_Intent) { 
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         * 
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(i_Intent);
    }
     
    private void handleIntent(Intent i_Intent) {
        String action = i_Intent.getAction();
        try{
        	if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {    
            String type = i_Intent.getType();
            	if (MIME_TEXT_PLAIN.equals(type)) {
            		Tag tag = i_Intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            		new NdefReaderTask().execute(tag);
            	} else {
            		Toast.makeText(this, "Wrong mime type: " + type, Toast.LENGTH_SHORT).show();
            	}
            }
        }
        catch(Exception ex)
        {
        	Toast.makeText(this, "Shaise", Toast.LENGTH_SHORT).show();
        }
        
        /*else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
             
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();
             
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }*/
    }
    
    /**
     * Background task for reading the data. Do not block the UI thread while reading. 
     * 
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
     
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
             
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag. 
                return null;
            }
     
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
     
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        
                    }
                }
            }
     
            return null;
        }
         
        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            /*
             * See NFC forum specification for "Text Record Type Definition" at 3.2.1 
             * 
             * http://www.nfc-forum.org/specs/
             * 
             * bit_7 defines encoding
             * bit_6 reserved for future use, must be 0
             * bit_5..0 length of IANA language code
             */
     
            byte[] payload = record.getPayload();
     
            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
     
            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;
             
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
             
            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }
         
       @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                _nfcDataTextView.setText("Read content: " + result);
            	connectToBTDevice(result); //can be disabled
            	InputStream input;
                try {
                	int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    InputStream instream = _socket.getInputStream();
                    int bytesRead = -1;
                    String message = "";
                    while (true) {
                        message = "";
                        bytesRead = instream.read(buffer);
                        if (bytesRead != -1) {
                            while ((bytesRead == bufferSize) && (buffer[bufferSize - 1] != 0)) {
                                message = message + new String(buffer, 0, bytesRead);
                                bytesRead = instream.read(buffer);
                            }
                            message = message + new String(buffer, 0, bytesRead - 1); // Remove
                            // the
                            // stop
                            // marker
                            Log.e("this", message);
                        }
                    }
                } catch (IOException e) {
                    Log.i("this",
                            "IOException in BtStreamWatcher - probably caused by normal disconnection",
                            e);
                }
            }
       }
    }
    
    private void connectToBTDevice(String i_MacAddr) {
    	
    	BluetoothSocket tmpSocket = null;
    	//i_MacAddr = "00:1B:B1:FC:D3:70"; // gils pc mac
    	try {
    		_btDevice = _btAdapter.getRemoteDevice(i_MacAddr);
    		Toast.makeText(getApplicationContext(), _btDevice.getName(), Toast.LENGTH_SHORT).show();
        	Method m = _btDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        	tmpSocket = (BluetoothSocket)m.invoke(_btDevice , Integer.valueOf(1));
    		//tmpSocket = _btDevice.createInsecureRfcommSocketToServiceRecord(_myUuid);
    		//tmpSocket = _btDevice.createRfcommSocketToServiceRecord(_myUuid);
    		//Method m = _btDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
    		//tmpSocket = (BluetoothSocket)m.invoke(_btDevice, 1);
    		}
    	catch(Exception ex)
    	{
    		ex.toString();
    	}
    	
    	_socket = tmpSocket;
    	try {
			_socket.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
     
    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
 
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
 
        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
         
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }
 
    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		
		return false;
	}
}
