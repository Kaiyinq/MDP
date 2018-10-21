import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.lohkaiying.mdpgrp13.MainActivity;

public class BluetoothLostReceiver extends BroadcastReceiver {

    MainActivity main = null;

    public void setMainActivity(MainActivity main)
    {
        this.main = main;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction()) )
        {

        }
    }
}
