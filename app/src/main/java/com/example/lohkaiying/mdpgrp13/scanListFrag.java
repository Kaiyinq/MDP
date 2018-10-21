package com.example.lohkaiying.mdpgrp13;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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


public class scanListFrag extends DialogFragment {


    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;
    private TextView selectedDevice;
    private Button connectButton;
    private bluetoothFragment.OnFragmentInteractionListener mListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_list, container, false);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        selectedDevice = (TextView)view.findViewById(R.id.selected_device);

        connectButton=(Button)view.findViewById(R.id.button_select);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(selectedDevice.getText().toString().isEmpty()){
                    selectedDevice.setText("please select device");
                }else{
                    String info = selectedDevice.getText().toString();
                    String address = info.substring(info.length() - 17);

                    ((MainActivity)getActivity()).connectDevice(address);
                }


            }
        });


        mNewDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.devicename);
        ListView newDevicesListView = (ListView)view.findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        doDiscovery();

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

            selectedDevice.setText(info);
            //  ((MainActivity)getActivity()).connectDevice(address);

            // Set result and finish this Activity

        }
    };


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();



            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "No device is found";
                    // mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        getActivity().unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {


        // Indicate scanning in the title


        // Turn on sub-title for new devices
        //getActivity(). findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle("New Device List");

    }
}
