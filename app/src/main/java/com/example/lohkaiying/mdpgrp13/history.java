package com.example.lohkaiying.mdpgrp13;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class history extends AppCompatActivity {

    ListView simpleList;
    public static List<String> historyList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        simpleList = (ListView)findViewById(R.id.simpleListView);


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.devicename, R.id.tvText, historyList);

        simpleList.setAdapter(arrayAdapter);

        TextView emptyText = (TextView)findViewById(android.R.id.empty);
            simpleList.setEmptyView(emptyText);

    }

    public static int getSize(){

       return historyList.size();
    }

    public static void populateListView(String msg){
        historyList.add(msg);
    }


    public static void renewListView(){
historyList.clear();


    }





}
