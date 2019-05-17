package com.gminney.triptracker_minney2019;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class TripListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.tripListFragmentContainer);
        if (fragment==null) {
            fragment = new TripListFragment();
            manager.beginTransaction()
                    .add(R.id.tripListFragmentContainer, fragment)
                    .commit();
        }
    }
}