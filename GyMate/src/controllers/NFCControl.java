package controllers;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.widget.TextView;

import com.example.test.R.string;

public class NFCControl{
	
	public enum eNfcStatus{
		NFC_NOT_INITIALIZED, NFC_OK, NFC_DISABLED, NFC_NOT_SUPPORTED;
	}
	public static final String MIME_TEXT_PLAIN = "text/plain";
	
	private Context _ctx;
	private NfcAdapter _nfcAdapter;
	private TextView _nfcDataTextView;

	public NfcAdapter get_nfcAdapter() {
		return _nfcAdapter;
	}

	public TextView get_nfcDataTextView() {
		return _nfcDataTextView;
	}

	public NFCControl(Context i_ctx) {
		_ctx = i_ctx;
	}

	public eNfcStatus initNFC() 
	{
		eNfcStatus resStatus = eNfcStatus.NFC_NOT_INITIALIZED;
		_nfcAdapter = NfcAdapter.getDefaultAdapter(_ctx);
		if (_nfcAdapter == null) 
		{
			resStatus = eNfcStatus.NFC_NOT_SUPPORTED;
		} 
		else if (!_nfcAdapter.isEnabled()) {
			resStatus = eNfcStatus.NFC_DISABLED;
		} else {
			resStatus = eNfcStatus.NFC_OK;
		}
		
		return resStatus;
	}
	
	public String handleIntent(Intent i_Intent) throws UnsupportedEncodingException {
		String action = i_Intent.getAction();
		String macAddress = null;
		
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			String type = i_Intent.getType();
			if (MIME_TEXT_PLAIN.equals(type)) {
				Tag tag = i_Intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				macAddress = getMacAddress(tag);
			}
		}
		
		return macAddress;
	}

	private String getMacAddress(final Tag tag) throws UnsupportedEncodingException {
		String resMacAddress = null;
        Ndef ndef = Ndef.get(tag);
		if (ndef != null) {
			NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			NdefRecord[] records = ndefMessage.getRecords();
			
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
						&& Arrays.equals(ndefRecord.getType(),
								NdefRecord.RTD_TEXT)) {
					resMacAddress = readText(ndefRecord);
					break;
				}
			}
		} 
 
        return resMacAddress;
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
    
    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public void setupForegroundDispatch(final Activity activity) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
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
         
        _nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }
    
    public static void stopForegroundDispatch(final Activity activity) {
        _nfcAdapter.disableForegroundDispatch(activity);
    }
}