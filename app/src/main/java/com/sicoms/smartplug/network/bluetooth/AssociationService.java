/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2014
 *
 *  This software is provided to the customer for evaluation
 *  purposes only and, as such early feedback on performance and operation
 *  is anticipated. The software source code is subject to change and
 *  not intended for production. Use of developmental release software is
 *  at the user's own risk. This software is provided "as is," and CSR
 *  cautions users to determine for themselves the suitability of using the
 *  beta release version of this software. CSR makes no warranty or
 *  representation whatsoever of merchantability or fitness of the product
 *  for any particular purpose or use. In no event shall CSR be liable for
 *  any consequential, incidental or special damages whatsoever arising out
 *  of the use of or inability to use this software, even if the user has
 *  advised CSR of the possibility of such damages.
 *
 ******************************************************************************/

package com.sicoms.smartplug.network.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.network.bluetooth.util.AssociationListener;
import com.sicoms.smartplug.network.bluetooth.util.AssociationStartedListener;
import com.sicoms.smartplug.network.bluetooth.util.DeviceController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 * Fragment used to discover and associate devices. Devices can be associated by tapping them or by pressing the QR code
 * button and scanning a QR code that will provide the UUID and authorisation code. If authorisation has been enabled on
 * the security settings screen then when a UUID is tapped the short code is prompted for.
 * 
 */
public class AssociationService implements AssociationListener, AssociationStartedListener {
    private static final int MAX_SHORT_CODE_LENGTH = 24;
    private DeviceController mController;
    private ArrayList<ScanInfo> mNewDevices = new ArrayList<ScanInfo>();
    private Button mQrButton;
 // Time to wait until check if the info of the devices is valid.
    private static final int CHECKING_SCANIFO_TIME_MS = (5 * 1000);

    //private UuidResultsAdapter resultsAdapter;
    private ProgressDialog mProgress = null;

    private int mRemovePosition;
    private boolean mPositionKnown;

    private BluetoothManager mBluetoothManager;
    private BLAssociationResultCallbacks mAssociationResultCallbacks;
    private Activity mActivity;

    public AssociationService(Activity activity, BluetoothManager bluetoothManager) {
        mActivity = activity;
        mBluetoothManager = bluetoothManager;
        try {
            mController = (DeviceController) bluetoothManager;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(bluetoothManager.toString() + " must implement DeviceController callback interface.");
        }
    }

    public void serviceAssociation() {
        mController.discoverDevices(true, this);
        startCheckingScanInfo();
    }

    /**
     * Start checking if the list of devices we are displaying contains a valid info or should be removed from the list.
     */
    private void startCheckingScanInfo() {
        mBluetoothManager.getMeshHandler().post(checkScanInfoRunnable);
	}
    
    /**
     * Stop checking if the list of devices we are displaying contains a valid info or should be removed from the list.
     */
    private void stopCheckingScanInfo(){
        mBluetoothManager.getMeshHandler().removeCallbacks(checkScanInfoRunnable);
    }

    public interface BLAssociationResultCallbacks {
        void onBLNonAssociationResult(String uuid, int uuidHsh, int rssi);
        void onBLAssociationResult(boolean isAssociated);
    }

    public void setOnBLAssociationResultCallbacks(final BLAssociationResultCallbacks callbacks){
        mAssociationResultCallbacks = callbacks;
    }

    public void stop() {
        mController.discoverDevices(false, null);
        stopCheckingScanInfo();
    }

    @Override
    public void newUuid(UUID uuid, int uuidHash, int rssi) {
        boolean existing = false;
        for (ScanInfo info : mNewDevices) {
            if (info.uuid.equalsIgnoreCase(uuid.toString())) {
                info.rssi = rssi;
                info.updated();
                //resultsAdapter.notifyDataSetChanged();
                existing = true;
                break;
            }
        }
        if (!existing) {
            mNewDevices.add(new ScanInfo(uuid.toString().toUpperCase(), rssi, uuidHash));        
            //resultsAdapter.notifyDataSetChanged();
        }

        for(ScanInfo info : mNewDevices) {
            mAssociationResultCallbacks.onBLNonAssociationResult(info.uuid, info.uuidHash, info.rssi);
        }
    }

    /**
     * Show modal progress dialogue whilst associating a device.
     */
    private void showProgress() {
        mActivity.setProgressBarIndeterminateVisibility(false);
        if(mProgress==null){
	        mProgress = new ProgressDialog(mActivity);
	        mProgress.setMessage(mActivity.getString(R.string.associating));
	        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        mProgress.setIndeterminate(false);
	        mProgress.setCancelable(false);
	        mProgress.show();
	        mProgress.setMessage("Sending association request");
        }
    }

    /**
     * Hide the progress dialogue when association is finished.
     */
    private void hideProgress() {
    	if(mProgress!=null){
    		mProgress.dismiss();
        	mProgress=null;
    	}
        mActivity.setProgressBarIndeterminateVisibility(true);
    }

    /**
     * Associate a device after first prompting for a short code.
     * 
     * @param hash
     *            The 31-bit UUID hash of the device to associate.
     * @param position
     *            Position of device in ListView.
     */
    private void associateShortCode(final int hash, final int position) {
        final AlertDialog.Builder shortCodeDialog = new AlertDialog.Builder(mActivity);
        shortCodeDialog.setTitle(mActivity.getString(R.string.short_code_prompt));
        final EditText input = new EditText(mActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_SHORT_CODE_LENGTH) });
        shortCodeDialog.setView(input);

        shortCodeDialog.setPositiveButton(mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (mController.associateDevice(hash & 0x7FFFFFFF, input.getText().toString())) {
                        mRemovePosition = position;
                        showProgress();
                    }
                    else {
                        Toast.makeText(mActivity, mActivity.getString(R.string.short_code_match_fail), Toast.LENGTH_LONG).show();
                    }
                }
                catch (IllegalArgumentException e) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.shortcode_invalid), Toast.LENGTH_LONG).show();
                }
            }
        });
        shortCodeDialog.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = shortCodeDialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;
            private boolean deletingBackward;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting)
                    return;

                // Make sure user is deleting one char, without a selection
                final int selStart = Selection.getSelectionStart(s);
                final int selEnd = Selection.getSelectionEnd(s);
                if (s.length() > 1 // Can delete another character
                        && count == 1 // Deleting only one character
                        && after == 0 // Deleting
                        && s.charAt(start) == '-' // a hyphen
                        && selStart == selEnd) { // no selection
                    deletingHyphen = true;
                    hyphenStart = start;
                    // Check if the user is deleting forward or backward
                    if (selStart == start + 1) {
                        deletingBackward = true;
                    }
                    else {
                        deletingBackward = false;
                    }
                }
                else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (isFormatting)
                    return;

                isFormatting = true;

                // If deleting hyphen, also delete character before or after it
                if (deletingHyphen && hyphenStart > 0) {
                    if (deletingBackward) {
                        if (hyphenStart - 1 < text.length()) {
                            text.delete(hyphenStart - 1, hyphenStart);
                        }
                    }
                    else if (hyphenStart < text.length()) {
                        text.delete(hyphenStart, hyphenStart + 1);
                    }
                }
                if ((text.length() + 1) % 5 == 0) {
                    text.append("-");
                }

                isFormatting = false;

                if (text.length() < MAX_SHORT_CODE_LENGTH) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
                else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

    }

    @Override
    public void deviceAssociated(boolean success) {
        hideProgress();
        if (!success) {
            Toast.makeText(mActivity, mActivity.getString(R.string.association_fail), Toast.LENGTH_SHORT).show();

        }
        else if (mPositionKnown) {
        	// prevent a crash.
        	if(mNewDevices.size()>mRemovePosition){
        		mNewDevices.remove(mRemovePosition);
        	}
            //resultsAdapter.notifyDataSetChanged();
        }
        mAssociationResultCallbacks.onBLAssociationResult(success);
    }

    @Override
    public void associationStarted() {
        // Association was triggered by MainActivity, so display progress dialogue.
    	showProgress();
    }
    
//    private class UuidResultsAdapter extends BaseAdapter {
//        private Activity activity;
//        private ArrayList<ScanInfo> data;
//        private LayoutInflater inflater = null;
//
//        public UuidResultsAdapter(Activity a, ArrayList<ScanInfo> object) {
//            activity = a;
//            data = object;
//            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        }
//
//        public int getCount() {
//            return data.size();
//        }
//
//        public Object getItem(int position) {
//            return data.get(position);
//        }
//
//        public long getItemId(int position) {
//            return position;
//        }
//
//        public View getView(int position, View convertView, ViewGroup parent) {
//            TextView nameText;
//            TextView rssiText;
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.uuid_list_row, null);
//                nameText = (TextView) convertView.findViewById(R.id.ass_uuid);
//                rssiText = (TextView) convertView.findViewById(R.id.ass_rssi);
//                convertView.setTag(new ViewHolder(nameText, rssiText));
//            }
//            else {
//                ViewHolder viewHolder = (ViewHolder) convertView.getTag();
//                nameText = viewHolder.uuid;
//                rssiText = viewHolder.rssi;
//            }
//
//            ScanInfo info = (ScanInfo) data.get(position);
//            nameText.setText(info.uuid);
//            rssiText.setText(String.valueOf(info.rssi) + "dBm");
//
//            return convertView;
//        }
//
//        @Override
//        public void notifyDataSetChanged(){
//        	// before notify. sort the data by RSSI.
//        	Collections.sort(data);
//
//        	super.notifyDataSetChanged();
//        }
//    }
   
    private class ScanInfo implements Comparable<ScanInfo> {
        
    	private static final long TIME_SCANINFO_VALID = 5* 1000; // 5 secs
        
		public String uuid;
        public int rssi;
        public int uuidHash;
        public long timeStamp;
        public ScanInfo(String uuid, int rssi, int uuidHash) {
            this.uuid = uuid;
            this.rssi = rssi;
            this.uuidHash = uuidHash;
            updated();
        }
		public void updated() {
			this.timeStamp = System.currentTimeMillis();
		}
		@Override
		public int compareTo(ScanInfo info) {
			// return 
			if(this.rssi>info.rssi)
				return -1;
			else if(this.rssi<info.rssi)
				return 1;
			return 0;
		}
		
		/**
		 * This method check if the timeStamp of the last update is still valid or not (time<TIME_SCANINFO_VALID).
		 * @return true if the info is still valid
		 */
		public boolean isInfoValid(){
			return ((System.currentTimeMillis()-this.timeStamp)<TIME_SCANINFO_VALID);
		}
    }
    
    private static class ViewHolder {
        public final TextView uuid;
        public final TextView rssi;

        public ViewHolder(TextView uuid, TextView rssi) {
            this.uuid = uuid;
            this.rssi = rssi;
        }
    }
    static String TAG_PROGRESS="AssociationProgress";
	@Override
	public void associationProgress(final int progress, final String message) {
		
		// different cases that we should avoid to set the progress
		if(progress<0 || progress>100 || mProgress==null || !mProgress.isShowing()){
			return;
		}
		Log.d(TAG_PROGRESS, message);
		// run in the UI thread
		mActivity.runOnUiThread(new Runnable() {
		    public void run() {
		    	mProgress.setProgress(progress);
		    	// we don't change the message anymore
		    	//mProgress.setMessage(message);  
		    	
		    }
		});
	}
	
	/**
	 * Runnable which checks if the info of the list of the devices (scan info list) is still valid or should be removed from the list.
	 */
	private Runnable checkScanInfoRunnable = new Runnable() {
        @Override
        public void run() {
        	Iterator<ScanInfo> it = mNewDevices.iterator();
        	while(it.hasNext()){
        		ScanInfo info=it.next();
        		if(!info.isInfoValid()){
        			it.remove();
        		}
        	}
        	//resultsAdapter.notifyDataSetChanged();
        	mBluetoothManager.getMeshHandler().postDelayed(checkScanInfoRunnable, CHECKING_SCANIFO_TIME_MS);
        }        
    };    
}
