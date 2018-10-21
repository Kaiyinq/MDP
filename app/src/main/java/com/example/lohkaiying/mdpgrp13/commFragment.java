package com.example.lohkaiying.mdpgrp13;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class commFragment extends Fragment {

    private static final String TAG = "KeySettingActivity";
    private OnFragmentInteractionListener mListener;
    SharedPreferences spf;
    EditText editTextF1, editTextF2;
    Button buttonSave,buttonF1,buttonF2;;

    public commFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_comm, container, false);
        editTextF1 = (EditText) view.findViewById(R.id.editTextF1);
        editTextF2 = (EditText) view.findViewById(R.id.editTextF2);
        buttonF1 = (Button)view.findViewById(R.id.buttonF1);
        buttonF2 = (Button)view.findViewById(R.id.buttonF2);
        buttonSave = (Button) view.findViewById(R.id.buttonSave);
        spf = getActivity().getSharedPreferences(Constants.PREF_DB, Context.MODE_PRIVATE);

        editTextF1.setText(spf.getString(Constants.SET_CMD1, Constants.SET_CMD1_DEFAULT));
        editTextF2.setText(spf.getString(Constants.SET_CMD2, Constants.SET_CMD2_DEFAULT));
        buttonF1.setOnClickListener(commandButton);
        buttonF2.setOnClickListener(commandButton);

        buttonSave.setOnClickListener(commandButton);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return view;
    }

    private View.OnClickListener commandButton = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == buttonF1) {
                String c1 = editTextF1.getText().toString();
                mListener.sendMessage(c1,true);
            } else if (v == buttonF2) {
                String c2 = editTextF2.getText().toString();
                mListener.sendMessage(c2,true);
            }else if (v == buttonSave){
                savePrefs();
            }



        }
    };

    private void savePrefs() {

        String c1 = editTextF1.getText().toString();
        c1 = c1.length() > 0 ? c1 : Constants.SET_CMD1_DEFAULT;
        String c2 = editTextF2.getText().toString();
        c2 = c2.length() > 0 ? c2 : Constants.SET_CMD2_DEFAULT;

        SharedPreferences.Editor editor = spf.edit();

        editor.putString(Constants.SET_CMD1, c1);
        editor.putString(Constants.SET_CMD2, c2);

        editor.apply(); //commit()


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
        void sendMessage(String message, boolean ack) ;
    }
}
