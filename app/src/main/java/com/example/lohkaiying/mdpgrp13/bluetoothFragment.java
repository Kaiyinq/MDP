package com.example.lohkaiying.mdpgrp13;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class bluetoothFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public bluetoothFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private int REQUEST_ENABLE_BT = 99; // Any positive integer should work.
    Button scanButton;
    SharedPreferences spf;


    public static final String DEVICE_NAME = "device_name";
    // Message types sent from the BluetoothChatService Handler
    // Called to have the fragment instantiate its user interface view.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_bluetooth, container, false);
//        getActivity().requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        spf = getActivity().getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetoothOnDevice();
        // Set result CANCELED in case the user backs out
        getActivity().setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        scanButton = (Button) view.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                scanListFrag setFunct = new scanListFrag();
                setFunct.show(getFragmentManager(), "setFunct");


            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.devicename);



        ListView pairedListView = (ListView) view.findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);





        populateListView();
        return view;
    }
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address


            ((MainActivity)getActivity()).connectDevice(address);

            // Set result and finish this Activity

        }
    };
    // Called when a fragment is first attached to its context
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * OnFragmentInteractionListener handle communication between Activity
     * and Fragment using an interface (OnFragmentInteractionListener)
     * and is created by default by Android Studio, but if you dont need to
     * communicate with your activity, you can just get rid of it.
     * */
    public interface OnFragmentInteractionListener {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        //   getActivity().unregisterReceiver(mReceiver);
    }



    // The on-click listener for all devices in the ListViews






    private void enableBluetoothOnDevice() {
        if (mBtAdapter == null) {
            Log.e(TAG, "This device does not have a bluetooth adapter");
            getActivity().finish();
            // If the android device does not have bluetooth, just return and get out.
            // There's nothing the app can do in this case. Closing app.
        }

        // Check to see if bluetooth is enabled. Prompt to enable it
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void populateListView() {

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            mPairedDevicesArrayAdapter.clear();
            // getActivity().findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "No device is paired";
            // mPairedDevicesArrayAdapter.add(noDevices);
        }

    }

}
