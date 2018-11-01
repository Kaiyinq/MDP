package com.example.lohkaiying.mdpgrp13;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import static android.content.ContentValues.TAG;
import static com.example.lohkaiying.mdpgrp13.MainActivity.MapCoorXY;
import static com.example.lohkaiying.mdpgrp13.MainActivity.maze;

public class controllerFragment extends Fragment implements View.OnClickListener{

    // ROBOT VARIABLES
    private String direction;
    public static int[] robotFront = {1, 2};
    public static int[] robotCenter = {1, 1};

    // ARENA VARIABLES
    public static int[] obstacleArray;
    public static int[] exploredArray;
    public static int[] arrowArrayX;
    public static int[] arrowArrayY;
    public static char[] arrowArraySide;
    public static boolean isMapUnexplored = false;

    private ImageButton upBtn;
    private ImageButton downBtn;
    private ImageButton leftBtn;
    private ImageButton rightBtn;

    private Button beginExBtn;
    private Button beginFpBtn;

    private TextView robotStatus;

    // TIMER FOR EXPLORATION & FASTEST PATH
    private Button resetBtn;
    TextView stopwatchText1, stopwatchText2;
    Handler handler;
    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    private int Seconds, Minutes, MilliSeconds ;
    private boolean stopwatch1Clicked = false;
    private boolean stopwatch2Clicked = false;
    private boolean timerEXOn = false;
    private boolean timerFPOn = false;

    // MOTION SWITCH
    Switch controlswitch;
    private OnFragmentInteractionListener mListener;

    private SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    public controllerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPref = getActivity().getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        handler = new Handler();
        stopwatchText1 = (TextView) view.findViewById(R.id.stopwatchText1);
        stopwatchText2 = (TextView) view.findViewById(R.id.stopwatchText2);

        String exStoredValue = sharedPref.getString("ExTime", "");
        String fpStoredValue = sharedPref.getString("FpTime", "");
        stopwatchText1.setText(exStoredValue);
        stopwatchText2.setText(fpStoredValue);

        robotStatus = (TextView) view.findViewById(R.id.robotStatus);

        upBtn = view.findViewById(R.id.upBtn);
        upBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                robotStatus.setText("Moving Forward");
                up();
                mListener.sendMessage("W",true); // UP
            }
        });

        downBtn = view.findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotStatus.setText("Moving Backward");
                down();
                mListener.sendMessage("S",true); // DOWN
            }
        });

        leftBtn = view.findViewById(R.id.leftBtn);
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotStatus.setText("Turning Left");
                left();
                mListener.sendMessage("A",true); // LEFT
            }
        });

        rightBtn= view.findViewById(R.id.rightBtn);
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotStatus.setText("Turning Right");
                right();
                mListener.sendMessage("D",true); // RIGHT
            }
        });

        beginExBtn = view.findViewById(R.id.beginExplorationBtn);
        beginExBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (timerFPOn = true) {
                    timerFPOn = false;
                    stopwatch2Clicked = false;
                    handler.removeCallbacks(runnable);

                    timerEXOn = !(timerEXOn);
                    if (timerEXOn) {
                        mListener.sendMessage("EX_START", true); // to let algo team know i started Exploration Path
                        StartTime = SystemClock.uptimeMillis();
                        handler.postDelayed(runnable, 0);
                        stopwatch1Clicked = true;
                        MainActivity.ExHistory = true;
                        history.populateListView("Exploration Path");
                    } else {
                        stopwatch1Clicked = false;
                        //handler.removeCallbacks(runnable);
                    }
                }

            }
        });

        beginFpBtn = view.findViewById(R.id.beginFastestPathBtn);
        beginFpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (timerEXOn = true) {
                    timerEXOn = false;
                    stopwatch1Clicked = false;
                    handler.removeCallbacks(runnable);

                    timerFPOn =! (timerFPOn);
                    if (timerFPOn) {
                        mListener.sendMessage("FP_START", true); // to let algo team know i started Fastest Path
                        StartTime = SystemClock.uptimeMillis();
                        handler.postDelayed(runnable, 0);
                        stopwatch2Clicked = true;
                        //history.populateListView("Fastest Path");
                    }else {
                        stopwatch2Clicked = false;
                        //handler.removeCallbacks(runnable);
                    }
                }


            }
        });


        //reset button
        resetBtn = view.findViewById(R.id.resetBtn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if(history.getSize()!=0) {
//                    history.renewListView();
//                }

                stopwatch1Clicked = false;
                stopwatch2Clicked = false;
                handler.removeCallbacks(runnable);

                stopwatchText1.setText("00:00:00");
                stopwatchText2.setText("00:00:00");

                editor.putString("ExTime", "00:00:00");
                editor.putString("FpTime", "00:00:00");
                editor.apply();

                clearArrays();
                updateRobotCoordsdir(robotCenter[0], robotCenter[1], robotFront[0], robotFront[1], "update");
            }
        });


        controlswitch = (Switch) view.findViewById(R.id.simpleSwitch);
        controlswitch.setChecked(false);
        controlswitch .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(controlswitch.isChecked()){
                    MainActivity.tipMode=true;
                }else{
                    MainActivity.tipMode=false;
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void left(){
        direction = ("left");
        // PASSING THE ORIGNAL START POINT WHICH IS [1,1] FOR ROBOT CENTER AND [1,2] FOR ROBOT FRONT
        // IF THERE IS NO UPDATE OF START POINT, UPDATE
        updateRobotCoordsdir(robotCenter[0], robotCenter[1], robotFront[0], robotFront[1], direction);
    }

    public void right(){
        direction = ("right");
        updateRobotCoordsdir(robotCenter[0], robotCenter[1], robotFront[0], robotFront[1], direction);
    }

    public void up(){
        direction = ("up");
        updateRobotCoordsdir(robotCenter[0], robotCenter[1], robotFront[0], robotFront[1], direction);
    }

    public void down(){
        direction = ("down");
        updateRobotCoordsdir(robotCenter[0], robotCenter[1], robotFront[0], robotFront[1], direction);
    }

    public static void updateRobotCoordsdir(int robotCenterX, int robotCenterY, int robotFrontX, int robotFrontY, String direction) {
        robotCenter[0] = robotCenterX;
        robotCenter[1] = robotCenterY;
        robotFront[0] = robotFrontX;
        robotFront[1] = robotFrontY;

//        System.out.println("centreX: " + robotCenter[0] + " centreY: " + robotCenter[1]);
//        System.out.println("frontX: " + robotFront[0] + " frontY: " + robotFront[1]);

        switch (direction) {
            case "left":
                if (robotFront[1] != robotCenter[1]) {                                                   // ROBOT FACING UP OR DOWN WILL TURN LEFT
                    robotFront[0] = robotCenter[0] - 1;
                    robotFront[1] = robotCenter[1];
                }else if (robotFront[0] > robotCenter[0] && robotFront[1] == robotCenter[1]) {           // ROBOT FACING RIGHT WILL TURN LEFT
                    robotFront[0] = robotCenter[0] - 1;
                    robotFront[1] = robotCenter[1];
                }else if (robotFront[0] == (robotCenter[0] - 1)) {                                       // ROBOT ALREADY LEFT CONTINUE LEFT
                    if (robotCenter[0] > 1 && robotCenter[0] < 14) {
                        if (robotCenter[1] > 0 && robotCenter[1] < 19) {
                            robotCenter[0] = robotCenter[0] - 1;
                            robotFront[0] = robotFront[0] - 1;
                        }
                    }
                }
                break;
            case "right":
                if (robotFront[1] != robotCenter[1]) {                                                   // ROBOT FACING UP OR DOWN WILL TURN RIGHT
                    robotFront[0] = robotCenter[0] + 1;
                    robotFront[1] = robotCenter[1];
                }else if (robotFront[0] < robotCenter[0] && robotFront[1] == robotCenter[1]) {           // ROBOT FACING LEFT WILL TURN RIGHT
                    robotFront[0] = robotCenter[0] + 1;
                    robotFront[1] = robotCenter[1];
                }else if (robotFront[0] == (robotCenter[0] + 1)) {                                       // ROBOT ALREADY RIGHT CONTINUE RIGHT
                    if (robotCenter[0] > 0 && robotCenter[0] < 13) {
                        if (robotCenter[1] > 0 && robotCenter[1] < 19) {
                            robotCenter[0] = robotCenter[0] + 1;
                            robotFront[0] = robotFront[0] + 1;
                        }
                    }
                }
                break;
            case "up":
                if (robotFront[0] != robotCenter[0]) {                                                   // ROBOT FACING RIGHT OR LEFT WILL TURN TO FACE UP
                    robotFront[0] = robotCenter[0];
                    robotFront[1] = robotCenter[1] + 1;
                }else if (robotFront[1] < robotCenter[1] && robotFront[0] == robotCenter[0]) {           // ROBOT FACING DOWN WILL TURN TO FACE UP
                    robotFront[0] = robotCenter[0];
                    robotFront[1] = robotCenter[1] + 1;
                }else if (robotFront[0] == robotCenter[0]) {                                             // ROBOT ALREADY UP CONTINUE UP
                    if (robotCenter[0] > 0 && robotCenter[0] < 14) {
                        if (robotCenter[1] > 0 && robotCenter[1] < 18) {
                            robotCenter[1] = robotFront[1];
                            robotFront[1] = robotFront[1] + 1;
                        }
                    }
                }
                break;
            case "down":
                if (robotFront[0] != robotCenter[0]) {                                                   // ROBOT FACING RIGHT OR LEFT WILL TURN TO FACE DOWN
                    robotFront[0] = robotCenter[0];
                    robotFront[1] = robotCenter[1] - 1;
                }else if (robotFront[1] > robotCenter[1] && robotFront[0] == robotCenter[0]) {           // ROBOT FACING UP WILL TURN TO FACE DOWN
                    robotFront[0] = robotCenter[0];
                    robotFront[1] = robotCenter[1] - 1;
                }else if (robotFront[0] == (robotCenter[0])) {                                       // ROBOT ALREADY DOWN CONTINUE DOWN
                    if (robotCenter[0] > 0 && robotCenter[0] < 14) {
                        if (robotCenter[1] > 1 && robotCenter[1] < 19) {
                            robotCenter[1] = robotFront[1];
                            robotFront[1] = robotFront[1] - 1;
                        }
                    }
                }
                break;

            // UPDATE OF START POINT THEN STORE INTO THE ARRAYS
            // IT WILL THEN MOVE BASED ON THE APPOINTED START POINT COORDINATES
            case "update":
                robotCenter[0] = robotCenterX;
                robotCenter[1] = robotCenterY;
                robotFront[0] = robotFrontX;
                robotFront[1] = robotFrontY;
                break;
        }

        if (MainActivity.isAutoUpdateToggled) {
            maze.invalidate();
        }
    }

    public static void updateArrays(int[] expArray, int[] obsArray, int[] obsArrowX, int[] obsArrowY, char[] obsArrowSide) {
        exploredArray = expArray;
        obstacleArray = obsArray;
        arrowArrayX = obsArrowX;
        arrowArrayY = obsArrowY;
        arrowArraySide = obsArrowSide;

        if (MainActivity.isAutoUpdateToggled) {
            maze.invalidate();
        }
    }

    public static void setMapUnexplored(boolean b) {
        isMapUnexplored = b; //Map unexplored set to true
        clearArrays();
        maze.invalidate();
    }

    public static void clearArrays() {
        robotFront[0] = 1;  //x value
        robotFront[1] = 2;  //y value

        robotCenter[0] = 1; //x value
        robotCenter[1] = 1; //y value

        exploredArray = null;
        obstacleArray = null;
        arrowArrayX = null;
        arrowArrayY = null;

        for (int i = 0; i < Constants.COL; i++) {
            for (int j = 0; j < Constants.ROW; j++) {
                MapCoorXY[i][j].setWaypoint(false);
            }
        }

        editor.putString("wpCoorX", "--");
        editor.putString("wpCoorY", "--");
        editor.putString("spCoorX", "--");
        editor.putString("spCoorY", "--");
        editor.apply();
    }

    // TIMER
    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) ((UpdateTime % 1000)/10);

            String time = String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds) + ":" + String.format("%02d", MilliSeconds);

            if (stopwatch1Clicked) {
                stopwatchText1.setText(time);
                editor.putString("ExTime", time);
                editor.apply();
            } else if (stopwatch2Clicked) {
                stopwatchText2.setText(time);
                editor.putString("FpTime", time);
                editor.apply();
            }

            handler.postDelayed(this, 0);
        }

    };


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

    @Override
    public void onClick(View v) {

    }

    public interface OnFragmentInteractionListener {
        void sendMessage(String message, boolean ack);
    }
}