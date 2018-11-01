package com.example.lohkaiying.mdpgrp13;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

import static com.example.lohkaiying.mdpgrp13.Constants.MESSAGE_DEVICE_NAME;
import static com.example.lohkaiying.mdpgrp13.Constants.MESSAGE_READ;
import static com.example.lohkaiying.mdpgrp13.Constants.MESSAGE_STATE_CHANGE;
import static com.example.lohkaiying.mdpgrp13.Constants.MESSAGE_TOAST;
import static com.example.lohkaiying.mdpgrp13.Constants.MESSAGE_WRITE;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener,
        controllerFragment.OnFragmentInteractionListener,
        mapFragment.OnFragmentInteractionListener,
        bluetoothFragment.OnFragmentInteractionListener,
        commFragment.OnFragmentInteractionListener {

    public static mapArena maze;
    private RelativeLayout map;
    private double[] lastTouchDownXY = new double[2];
    private int CoorX, CoorY;
    public static Point touchCoor;
    public static mapCell[][] MapCoorXY = new mapCell[Constants.COL][Constants.ROW];
    public static final String TAG = "MainActivity";
    // AUTO TOGGLE BUTTON TRUE - MAZE AUTO UPDATE
    public static boolean isAutoUpdateToggled = true;
    private static String mdfExploredString = "";

    private FragmentTabHost mTabHost;
    private static controllerFragment controlFragment;
    private static mapFragment mapFragment;
    private static bluetoothFragment btFragment;
    private static commFragment commFragment;
    private String mConnectedDeviceName = null;

    // Bluetooth var
    private static boolean D = true;
    private TextView connectionStatus;
    SharedPreferences spf;
    private BluetoothService mChatService = null;
    private BluetoothAdapter mBluetoothAdapter;

    //C11 var
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;

    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax;

    // MOTION SWITCH
    public static boolean tipMode;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public static boolean ExHistory = false;

    // TESTING
    // and; 13,1,W FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF 01C00000000000000001F84000800100000003C20084000A000400088800F00000000000080 0,10,D;3,15,D;7,19,D;14,14,L;9,0,U;
    // ROBOT COOR: 1,18,A
    // EXPLOREDSTRING HEX: FFFFFFFFFFFFFFFFC1FF81FF01FE03FC07F80FF01FE03FC07F80FF81FF83FFFFFFFFFFFFFFFF
    // OBSTACLESTRING HEX: 000000080000020100101010103030202020200201000000400000000000000000000000000
    // ARROWSTRING HEX: 11,14;11,4;9,0;11,10;4,15;1,1;3,7;9,4;3,10;11,9;13,18;5,19;5,3;3,6;9,16;11,13;10,4;

//    public static String mdfExploredString = "";
//    public static String mdfObstacleString = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        init();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        // setup the tab content in tab host
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("Bluetooth").setIndicator("Bluetooth", null),
                bluetoothFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Communication").setIndicator("Communication", null),
                commFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Map Control").setIndicator("Map", null),
                mapFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Direction Control").setIndicator("Controller", null),
                controllerFragment.class, null);
        connectionStatus = (TextView)findViewById(R.id.bluetoothStatusTextView);

        //Bluetooth
        connectionStatus = (TextView)findViewById(R.id.bluetoothStatusTextView);
        mChatService = new BluetoothService(MainActivity.this, mHandler);
        //C11
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor , SensorManager.SENSOR_DELAY_NORMAL);


    }

    // Called when a fragment is attached as a child of this fragment.
    @Override
    public void onAttachFragment(android.support.v4.app.Fragment attachedFragment) {
        super.onAttachFragment(attachedFragment);

        if (attachedFragment.getClass().equals((bluetoothFragment.class))) {
            btFragment = (bluetoothFragment) attachedFragment;
        }
        if (attachedFragment.getClass().equals((commFragment.class))) {
            commFragment = (commFragment) attachedFragment;
        }
        if (attachedFragment.getClass().equals((controllerFragment.class))) {
            controlFragment = (controllerFragment) attachedFragment;
        }
        if (attachedFragment.getClass().equals((mapFragment.class))) {
            mapFragment = (mapFragment) attachedFragment;
        }

    }

    private void init() {
        // initializing of 2D array
        // waypoint all set to false
        for (int i = 0; i < Constants.COL; i++) {
            for (int j = 0; j < Constants.ROW; j++) {
                MapCoorXY[i][j] = new mapCell();
                MapCoorXY[i][j].setWaypoint(false);
            }
        }
        editor.putString("ExTime", "00:00:00");
        editor.putString("FpTime", "00:00:00");
        editor.putString("wpCoorX", "--");
        editor.putString("wpCoorY", "--");
        editor.putString("spCoorX", "--");
        editor.putString("spCoorY", "--");
        editor.apply();

        // to add the created mapArena to the .xml
        maze = new mapArena(this);
        map = findViewById(R.id.map);
        map.addView(maze);

        // to get the coordinates when user touch the screen
        map.setOnTouchListener(touchListener);

    }

    // the purpose of the touch listener is just to store the touch X,Y coordinates
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // save the X,Y coordinates
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastTouchDownXY[0] = event.getX();
                lastTouchDownXY[1] = event.getY();
            }

            //convert double to int
            int xx = (int) lastTouchDownXY[0];
            int yy = (int) lastTouchDownXY[1];

            // minus the padding
            // to know where I am exactly touching on the map arena
            if (xx > 175 && xx < ((mapArena.cellSize*Constants.COL) + 175)) {
                CoorX = xx - 175;
            } else {
                CoorX = -1;
            }
            if (yy > 10 && yy < ((mapArena.cellSize*Constants.ROW) + 10)) {
                CoorY = yy - 10;
            } else {
                CoorY = -1;
            }

            //System.out.println("cx:" + CoorX + ",cy:" + CoorY);

            if (CoorX != -1 && CoorY != -1) {
                CoorX = CoorX / mapArena.cellSize;
                CoorY = CoorY / mapArena.cellSize;
                System.out.println("CoorX: " + CoorX + " CoorY: " + CoorY);
                touchCoor = new Point(CoorX, CoorY);
                //System.out.println(touchCoor);

                // when cell selected, set to true
                MapCoorXY[CoorX][CoorY].setColor(true);

                // make the map rerun
                maze.invalidate();

            } else {
                Toast.makeText(MainActivity.this, "Please select cell within the maze box.", Toast.LENGTH_SHORT).show();
            }

            return false;
        }
    };

    // MANUALLY UPDATE THE WAYPOINT/ROBOT SEND BY ALGO TEAM??
    @Override
    public void manualUpdate() {
        //PASS MDF STRING HERE
        if(mdfExploredString.length() > 0) {
            controllerFragment.setMapUnexplored(true);
        }
        // make the map rerun
        maze.invalidate();
    }

    @Override
    public void toggleAutoUpdate(boolean isChecked) {
        //PASS MDF STRING HERE
        if(mdfExploredString.length() > 0) {
            controllerFragment.setMapUnexplored(true);
        }
        isAutoUpdateToggled = isChecked;
    }

    // Converting hex to dec to bin
    private String convertHexToBin (String Hex) {
        // 5 Hex digit each time to prevent overflow
        // assuming that "FF08C" is sent
        String mdfStringBin = "";
        String bin = "";
        String partial;
        int pointer = 0;

        while (Hex.length() - pointer > 0) {
            partial = Hex.substring(pointer, pointer + 1);
            bin = Integer.toBinaryString(Integer.parseInt(partial, 16));
            for (int i = 0; i < 4 - bin.length(); i++) {
                mdfStringBin = mdfStringBin.concat("0");
            }
            mdfStringBin = mdfStringBin.concat(bin);
            pointer += 1;
        }

        return mdfStringBin;
    }

    public void handleMDFString (String mdfString) {
        // assuming that "FF08C(first part) 000001(second part) x,y,1;(third part)" is sent

        //SPILT THE MDFSTRING TO THE 4 PARTS (ROBOT COORDINATES - X,Y,DIRECTION;, UNEXPLORED(0)/EXPLORED(1), EXPLORED(0)/OBSTACLES(1), OBSTACLES W ARROWS- X,Y;)
        String splitMDFString[] = mdfString.split(" ");

        // First part of mdfString X,Y,DIRECTION;
        String robotCoor[];
        int centerCol, centerRow, frontRow = 0, frontCol = 0;
        String direction;

        // Second part of mdfString 0 = explored, 1 = unexplored
        String exploredString;
        int exploredArray[] = null;

        // Third part of mdfString on obstacles, 0 = explored, 1 = obstacles
        String obstacleString;
        int obstacleArray[] = null;

        // Fourth part of mdfString on obstacles with arrow, X,Y;
        String obsArrow[];
        String arrowCoorXY[] = null;
        int arrowX[] = null;
        int arrowY[] = null;
        char arrowSide[] = null;

        if (splitMDFString[0].contains("and")) {

            // Add mdfString to the history
            if (ExHistory) {
                history.populateListView("MDFString: " + splitMDFString[2] + " " + splitMDFString[3]+ " " + splitMDFString[4]);
            }

            //ROBOT COORDINATES
            if (!splitMDFString[1].equals(" ")) {
                robotCoor = splitMDFString[1].split(",");
                centerCol = Integer.parseInt(robotCoor[0]);
                centerRow= Constants.ROW - 1 - Integer.parseInt(robotCoor[1]);
                direction = robotCoor[2];

                //System.out.println("CenterCol: " + centerCol + " CentreRow: " + centerRow);

                if (direction.contains("W")) {              // UP
                    frontRow = centerRow + 1;
                    frontCol = centerCol;
                } else if (direction.contains("S")) {       // DOWN
                    frontRow = centerRow - 1;
                    frontCol = centerCol;
                } else if (direction.contains("D")) {       // RIGHT
                    frontRow = centerRow;
                    frontCol = centerCol + 1;
                } else if (direction.contains("A")) {       // LEFT
                    frontRow = centerRow;
                    frontCol = centerCol - 1;
                }

                controllerFragment.updateRobotCoordsdir(centerCol, centerRow, frontCol, frontRow, "update");

            } else {
                //Toast.makeText(this, "No Robot Coor", Toast.LENGTH_SHORT).show();
            }

            //EXPLORE & UNEXPLORED
            if (!splitMDFString[2].equals(" ")) {
                exploredString = convertHexToBin(splitMDFString[2]);
                exploredArray = new int[exploredString.length() - 4];
                for (int i = 0; i < exploredArray.length; i++) {
                    // Remove the first two bits (11) and last two bits (11)
                    exploredArray[i] = Integer.parseInt(exploredString.substring(i + 2, i + 3));
                }

            } else {
                //Toast.makeText(this, "No Explored MDF String", Toast.LENGTH_SHORT).show();
            }

            System.out.println("ExploredArray: " + Arrays.toString(exploredArray));

            //EXPLORE & OBSTACLES
            if (!splitMDFString[3].equals(" ")) {
                obstacleString = convertHexToBin(splitMDFString[3]);
                obstacleArray = new int[obstacleString.length()];
                for (int i = 0; i < obstacleArray.length; i++) {
                    obstacleArray[i] = Integer.parseInt(obstacleString.substring(i, i + 1));
                }
            } else {
                //Toast.makeText(this, "No Obstacle MDF String", Toast.LENGTH_SHORT).show();
            }

            System.out.println("ObstacleArray: " + Arrays.toString(obstacleArray));

            //OBSTACLES WITH ARROWS
            if (!splitMDFString[4].contains("null")) { // "13,1;15,1;19,1;"
                int arrowPointer = 0;
                //System.out.println("A11: " + splitMDFString[4]);
                String newMDFString = splitMDFString[4].replace("\n", "").replace("\r", "");
                //System.out.println("A22: " + newMDFString);
                obsArrow = newMDFString.split(";"); // "13,1","15,1","19,1"

                arrowX = new int[obsArrow.length];
                arrowY = new int[obsArrow.length];
                arrowSide = new char [obsArrow.length];

                System.out.println(Arrays.toString(obsArrow));

                for (int i = 0; i < obsArrow.length; i++) {
                    arrowCoorXY = obsArrow[i].split(",");

//                    System.out.println("arrowCoorXY: " + Arrays.toString(arrowCoorXY));
//                    System.out.println("arrowCoorXY: " + arrowCoorXY[0]);
//                    System.out.println("arrowCoorXY: " + arrowCoorXY[1]);

                    arrowX[i] = Integer.parseInt(arrowCoorXY[0]);
                    arrowY[i] = Integer.parseInt(arrowCoorXY[1]);
                    arrowSide[i] = arrowCoorXY[2].charAt(0);

                }

//                System.out.println("arrowX: " + Arrays.toString(arrowX));
//                System.out.println("arrowY: " + Arrays.toString(arrowY));
                System.out.println("arrowSide: " + Arrays.toString(arrowSide));

            } else {
                arrowX = null;
                arrowY = null;
                arrowSide = null;
                //Toast.makeText(this, "No Obstacle Arrow MDF String", Toast.LENGTH_SHORT).show();
            }

            controllerFragment.updateArrays(exploredArray, obstacleArray, arrowX, arrowY, arrowSide);
        }
    }

    //BLUETOOTH METHOD
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    //Bluetooth Method
    /**
     * The Handler that gets information back from the BluetoothService
     */

    @SuppressLint("HandlerLeak")
    private final android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
                            connectionStatus.setText("connected to "+ mConnectedDeviceName);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "connectting", Toast.LENGTH_SHORT).show();
                            connectionStatus.setText("connectting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Toast.makeText(MainActivity.this, "listening", Toast.LENGTH_SHORT).show();
                            connectionStatus.setText("listening");
                            break;
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(MainActivity.this, " not connected", Toast.LENGTH_SHORT).show();
                            connectionStatus.setText("not connected");
                            break;
                        case BluetoothService.STATE_DISCONNECTED:
                            Toast.makeText(MainActivity.this, " not connected", Toast.LENGTH_SHORT).show();
                            connectionStatus.setText("Disconnected");

                            if(findDevice(getAddress())==true){
                                // connectDevice(getAddress());
                            }else{
                                connectionStatus.setText("Manual Connection");
                            }
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "not connected", Toast.LENGTH_SHORT).show();
                            connectionStatus.setText("not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(D) {
                        Log.i(TAG, "Mdf: " + readMessage);

                        handleMDFString(readMessage);
                        //Toast.makeText(MainActivity.this, "Receive from BT: " + readMessage, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    /**
     * Save the paired device id and start the initialisation of connection
     * @param address
     * @return
     */


    public boolean connectDevice(String address) {

        spf = getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = spf.edit();

        editor.putString(Constants.SET_ADDRESS, address);
        editor.apply();
        BluetoothDevice device = getDevice(address);
        if (mChatService.getState() == BluetoothService.STATE_CONNECTED) {

            return false;
        }

        if (device != null){
            mChatService.connect(device, true);
            return true;
        }
        else{
            Toast.makeText(MainActivity.this, "Error in connecting", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Ensure the device bluetooth is on after connection lost
     * @param address
     * @return
     */
    public boolean findDevice(String address) {
        // doDiscovery();
        // Get a set of currently paired devices
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Boolean founddevice=false;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String add= device.getAddress();
                if (add.equals(address)) {
                    founddevice= true;
                }else{
                    founddevice= false;
                }
            }
        }
        return founddevice;
    }


    /**
     * Finding device object using the device id
     * @param address
     * @return
     *
     */
    public BluetoothDevice getDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        return device;
    }

    /**
     * Get the stored pair device Id from storage
     * @return
     */
    public String getAddress() {
        spf = getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);

        return  spf.getString(Constants.SET_ADDRESS, "a");

    }

    /**
     * Start the transmission of msg to other device
     * @param message
     * @param ack
     */
    @Override
    public void sendMessage(String message, boolean ack) {
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Log.d(TAG, "not connected" + " abc");
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            Log.d(TAG, "MSG sent: " + new String(send));
        }
    }

    public void setText(String message) {
        connectionStatus.setText(message);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && tipMode == true) {
            xAccel = event.values[0];
            yAccel = -event.values[1];

            controlFragment = (controllerFragment) getSupportFragmentManager().findFragmentByTag("Direction Control");
            if (xAccel > 2.5) {
                //clockwise
                if (controlFragment != null) {
                    controlFragment.left();
                    sendMessage("A", true);
                }
            } else if (xAccel < -2.5) {
                //anticlockwise
                if (controlFragment != null) {
                    controlFragment.right();
                    sendMessage("D", true);
                }
            }

            if (yAccel > 2.5) {
                //forward
                if (controlFragment != null) {
                    controlFragment.up();
                    sendMessage("W", true);
                }

            } else if (yAccel < -2.5) {
                //backward
                if (controlFragment != null) {
                    controlFragment.down();
                    sendMessage("S", true);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.history:
                Intent myIntent = new Intent(MainActivity.this, history.class);

                MainActivity.this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}