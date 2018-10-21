package com.example.lohkaiying.mdpgrp13;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.SharedPreferences;

import static com.example.lohkaiying.mdpgrp13.MainActivity.MapCoorXY;
import static com.example.lohkaiying.mdpgrp13.MainActivity.maze;
import static com.example.lohkaiying.mdpgrp13.MainActivity.touchCoor;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.robotCenter;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.updateRobotCoordsdir;


public class mapFragment extends Fragment implements View.OnClickListener {

    private Button SPBtn;
    private Button WPBtn;

    private Button manualUpdateBtn;
    private ToggleButton autoUpdateBtn;

    private static TextView SPCoor;
    private static TextView WPCoor;

    //public static int[] waypointArray;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private OnFragmentInteractionListener mListener;

    public mapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPref = getActivity().getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        autoUpdateBtn = (ToggleButton) view.findViewById(R.id.autoUpdateBtn);
        autoUpdateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleAutoUpdate(buttonView, isChecked);
                if (isChecked){
                    // Toggle is enabled
                    manualUpdateBtn.setVisibility(View.INVISIBLE);
                }
                else{
                    // Toggle is disabled
                    manualUpdateBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        manualUpdateBtn = view.findViewById(R.id.manualUpdateBtn);
        manualUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manualUpdate();
            }
        });
        manualUpdateBtn.setVisibility(View.INVISIBLE);


        SPBtn = (Button) view.findViewById(R.id.startCoorBtn);
        WPBtn = (Button) view.findViewById(R.id.waypointBtn);
        SPBtn.setOnClickListener(this);
        WPBtn.setOnClickListener(this);

        SPCoor = (TextView) view.findViewById(R.id.SPCoorXY);
        WPCoor = (TextView) view.findViewById(R.id.WPCoorXY);

        String spStoredValueX = sharedPref.getString("spCoorX", "");
        String spStoredValueY = sharedPref.getString("spCoorY", "");
        SPCoor.setText("x:" + spStoredValueX + " y:" + spStoredValueY);

        String wpStoredValueX = sharedPref.getString("wpCoorX", "");
        String wpStoredValueY = sharedPref.getString("wpCoorY", "");
        WPCoor.setText("x:" + wpStoredValueX + " y:" + wpStoredValueY);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startCoorBtn:
                updateStartCoor();
                break;
            case R.id.waypointBtn:
                waypoint();
                break;
        }
    }

    // UPDATE OF THE ROBOT START POINT COORDINATES
    public void updateStartCoor () {
        if (touchCoor == null) {
            SPCoor.setText("x:-- y:--");
            Toast.makeText(getActivity(), "Please select cell within the maze box.", Toast.LENGTH_SHORT).show();
        } else {
            int x = touchCoor.x;
            int y = Constants.ROW - 1 - touchCoor.y;

            //System.out.println("x:" + x + "y:" + y);

            if (x == robotCenter[0] && y == robotCenter[1]) {
                // RESET ROBOT POSITION
                updateRobotCoordsdir(-2, -2, -1, -1, "update");
            } else {
                updateRobotCoordsdir(x, y, x, y+1, "update");
            }

            editor.putString("spCoorX", Integer.toString(touchCoor.x));
            editor.putString("spCoorY", Integer.toString(touchCoor.y));
            editor.apply();

            SPCoor.setText("x:" + touchCoor.x + " y:" + touchCoor.y);

            mListener.sendMessage("sp:" + touchCoor.x + "," + touchCoor.y, true);

            // IF AUTO TOGGLE BUTTON IS ON, MAZE RERUN
            if (MainActivity.isAutoUpdateToggled) {
                maze.invalidate();
            }

        }
    }

    // SET WAYPOINT
    public void waypoint () {
        if (touchCoor == null) {
            //System.out.println(MainActivity.touchCoor);
            WPCoor.setText("x:-- y:--");
            //MapCoorXY[-1][-1].setWaypoint(true);
            Toast.makeText(getActivity(), "Please select cell within the maze box.", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < Constants.COL; i++) {
                for (int j = 0; j < Constants.ROW; j++) {
                    if (MapCoorXY[i][j].isWaypoint() == true) {
                        Point existWP = new Point(i,j);
                        if (!existWP.equals(touchCoor)) {
                            MapCoorXY[i][j].setWaypoint(false);
                        }
                    }
                }
            }
            MapCoorXY[touchCoor.x][touchCoor.y].setWaypoint(true);

            editor.putString("wpCoorX", Integer.toString(touchCoor.x));
            editor.putString("wpCoorY", Integer.toString(touchCoor.y));
            editor.apply();

            WPCoor.setText("x:" + touchCoor.x + " y:" + touchCoor.y);


            mListener.sendMessage("wp:" + touchCoor.x + "," + touchCoor.y, true);
        }
    }

    public void manualUpdate() {
        if (mListener != null) {
            mListener.manualUpdate();
        }
    }

    public void toggleAutoUpdate(CompoundButton compoundButton, boolean isChecked) {
        if (mListener != null) {
            mListener.toggleAutoUpdate(isChecked);
        }
    }

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

    public interface OnFragmentInteractionListener {
        void manualUpdate();
        void toggleAutoUpdate(boolean isChecked);
        void sendMessage(String message, boolean ack);
    }
}
