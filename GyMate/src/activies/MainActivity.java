package activies;

import java.io.UnsupportedEncodingException;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.example.test.R;
import controllers.BluetoothController;
import controllers.BluetoothController.eBluetoothStatus;
import controllers.NFCControl;

public class MainActivity extends Activity {

	private NFCControl _nfcController;
	private BluetoothController _btConroller;
	private TextView _nfcDataTextView;
	//private UUID _myUuid = UUID.randomUUID();
	//private NfcAdapter _nfcAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		_nfcController = new NFCControl(this);
		_btConroller = new BluetoothController(this);
		
			switch (_nfcController.initNFC()){
				case NFC_DISABLED:
					startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
					_nfcController.initNFC();
					break;
				case NFC_NOT_SUPPORTED:
					Toast.makeText(this, R.string.nfc_not_supported, Toast.LENGTH_SHORT).show();
					finish();
					break;
				case NFC_OK:
					_nfcDataTextView = (TextView)findViewById(R.id.nfcDataText);
					_nfcDataTextView.setText(R.string.nfc_ready);
					
					if(_btConroller.initBT() == eBluetoothStatus.BT_DISABLED)
					{
						_btConroller.get_btAdapter();
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, 1);
					}
					break;
				default:
			}
			
			try {
				connectToBtDevice(_nfcController.handleIntent(getIntent()));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			}

	@Override
    protected void onResume() {
        super.onResume();
         
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown. 
         */
        _nfcController.setupForegroundDispatch(this);
    }
     
    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        _nfcController.stopForegroundDispatch(this);
         
        super.onPause();
    }
    
     
    @Override
    protected void onNewIntent(Intent i_Intent) { 
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the docum entation.
         * 
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        try {
        	connectToBtDevice(_nfcController.handleIntent(i_Intent));
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(this, "Unsupported encoding", Toast.LENGTH_LONG).show();
		}
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
    
    public void connectToBtDevice(String macAddress)
    {
    	if(macAddress == null)
    	{
    		Toast.makeText(this, "The tag returned null", Toast.LENGTH_SHORT).show();
    	}
    	else if (macAddress!= null) {
            _nfcDataTextView.setText("Read content: " + macAddress);
            _btConroller.connectToBTDevice(macAddress); 
    	}
    }
}