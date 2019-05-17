package com.gminney.triptracker_minney2019;

import android.os.Bundle;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;


public class TripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.tripFragmentContainer);
        if (fragment==null) {
            fragment = new TripFragment();
            manager.beginTransaction()
                    .add(R.id.tripFragmentContainer, fragment)
                    .commit();
        }
    }
}