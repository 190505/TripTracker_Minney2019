package com.gminney.triptracker_minney2019;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.List;

public class TripListFragment extends ListFragment {

    private static final String TAG = "TripListFragment";
    private ArrayList<Trip> mTrips;
    private boolean mPublicView = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPublicView = getActivity().getIntent().getBooleanExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, false);
        if (mPublicView)
            getActivity().setTitle(R.string.action_public_trips);
        else
            getActivity().setTitle(R.string.action_my_trips);
        mTrips = new ArrayList<Trip>();
        refreshTripList();
        TripAdapter adapter = new TripAdapter(mTrips);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trip_list, parent, false);
        ListView listView = v.findViewById(android.R.id.list);
        registerForContextMenu(listView);
        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Trip trip = (Trip)(getListAdapter()).getItem(position);
        Intent intent = new Intent(getActivity(), TripActivity.class);
        intent.putExtra(Trip.EXTRA_TRIP_ID, trip.getObjectId());
        intent.putExtra(Trip.EXTRA_TRIP_NAME, trip.getName());
        intent.putExtra(Trip.EXTRA_TRIP_DESC, trip.getDescription());
        intent.putExtra(Trip.EXTRA_TRIP_START_DATE, trip.getStartDate());
        intent.putExtra(Trip.EXTRA_TRIP_END_DATE, trip.getEndDate());
        intent.putExtra(Trip.EXTRA_TRIP_PUBLIC, trip.isShared());
        intent.putExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, mPublicView);
        startActivityForResult(intent, Activity.RESULT_OK);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_trip_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        TripAdapter adapter = (TripAdapter)getListAdapter();
        Trip trip = adapter.getItem(position);
        switch (item.getItemId()) {
            case R.id.menu_item_delete_trip:
                deleteTrip(trip);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_trips, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if (mPublicView) {
            menu.findItem(R.id.action_public_trips).setVisible(false);
            menu.findItem(R.id.action_my_trips).setVisible(true);
        } else {
            menu.findItem(R.id.action_public_trips).setVisible(true);
            menu.findItem(R.id.action_my_trips).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshTripList();
                return true;
            case R.id.action_new:
                intent = new Intent(getActivity(), TripActivity.class);
                intent.putExtra(Trip.EXTRA_TRIP_ID, "0");
                intent.putExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, mPublicView);
                startActivity(intent);
                return true;
            case R.id.action_public_trips:
                intent = new Intent(getActivity(), TripListActivity.class);
                intent.putExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, true);
                startActivity(intent);
                return true;
            case R.id.action_my_trips:
                intent = new Intent(getActivity(), TripListActivity.class);
                intent.putExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, false);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                Backendless.UserService.logout(new BackendlessCallback<Void>() {
                    @Override
                    public void handleResponse(Void v) {
                        boolean isValidLogin = Backendless.UserService.isValidLogin();
                        if (!isValidLogin) {
                            Log.i(TAG, "Successful logout");
                        }
                    }
                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.i(TAG, "Server reported an error " + backendlessFault.getMessage());
                    }
                });
                intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class TripAdapter extends ArrayAdapter<Trip> {
        public TripAdapter(ArrayList<Trip> trips) {
            super(getActivity(), 0, trips);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_trip_list_item, null);
            }
            Trip trip = getItem(position);
            TextView tripName = (TextView)convertView.findViewById(R.id.trip_list_item_textName);
            tripName.setText(trip.getName());
            Log.i(TAG, trip.getName());
            TextView tripStartDate = (TextView)convertView.findViewById(R.id.trip_list_item_textStartDate);
            tripStartDate.setText(DateFormat.format("MM-dd-yyyy", trip.getStartDate()));
            return convertView;

        }
    }

    private void deleteTrip(Trip trip) {
        if (trip.getOwnerId().equals(Backendless.UserService.loggedInUser())) {
            final Trip deleteTrip = trip;
            Thread deleteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Backendless.Data.of(Trip.class).remove(deleteTrip);
                    Log.i(TAG, deleteTrip.getName() + " removed.");
                    refreshTripList();
                }
            });
            deleteThread.start();
            try {
                deleteThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Deleting trip failed: " + e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(e.getMessage());
                builder.setTitle(R.string.delete_error_title);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.delete_error_message));
            builder.setTitle(getString(R.string.delete_error_title));
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            mPublicView = intent.getBooleanExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, false);
        }
    }

    private void refreshTripList() {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        DataQueryBuilder query = DataQueryBuilder.create();
        if (mPublicView){
            query.setWhereClause("shared = true");
        }
        else {
            query.setWhereClause("ownerId='" + user.getObjectId() + "'");
        }
        QueryOptions qo = new QueryOptions();
        qo.addSortByOption("startDate");
        //query.setQueryOptions(qo);

        Backendless.Persistence.of(Trip.class).find(query, new BackendlessCallback<List<Trip>>() {
            @Override
            public void handleResponse(List<Trip> response) {
                Log.d(TAG, response.get(0).toString());
                mTrips.clear();
                for (Trip trip : response){
                    mTrips.add(trip);
                }
                ArrayListSorter.insertionSort(mTrips);
                ((TripAdapter)getListAdapter()).notifyDataSetChanged();
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, fault.toString());
            }
        });
    }

    @Override
    public void onResume() {
        refreshTripList();
        super.onResume();
    }
}