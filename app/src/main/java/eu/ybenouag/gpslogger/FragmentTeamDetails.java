/*
 * FragmentAboutDialog - Java Class for Android
 * Created by G.Capelli on 26/7/2016
 * This file is part of BasicAirData GPS Logger
 *
 * Copyright (C) 2011 BasicAirData
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.ybenouag.gpslogger;

import static eu.ybenouag.gpslogger.GPSApplication.TOAST_VERTICAL_OFFSET;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * The About Dialog Fragment
 */
public class FragmentTeamDetails extends DialogFragment {

    private FrameLayout flTeamDetails;
    private EditText flCrewNameText;
    private Spinner flTeamDivisionDropDown;

    private final TeamDetailsManager teamDetailsManager = TeamDetailsManager.getInstance();
    private final GPSApplication gpsApplication = GPSApplication.getInstance();
    private ArrayAdapter<String> dataAdapter;

    public FragmentTeamDetails() {
        // Required empty public constructor
    }

    ViewTreeObserver.OnGlobalLayoutListener viewTreeObserverOnGLL = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                flTeamDetails.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
                flTeamDetails.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }

            int viewHeight = flTeamDetails.getMeasuredHeight() + (int) (6 * getResources().getDisplayMetrics().density);
            int layoutHeight = flTeamDetails.getHeight() - (int) (6 * getResources().getDisplayMetrics().density);
            boolean isTimeAndSatellitesVisible;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                isTimeAndSatellitesVisible = layoutHeight >= 6 * viewHeight;
                //Log.w("myApp", "[#] FragmentGPSFix MEASURED: " + layoutHeight + " / " + 6*viewHeight + " -> " + isTimeAndSatellitesVisible);
            } else {
                isTimeAndSatellitesVisible = layoutHeight >= 3.9 * viewHeight;
                //Log.w("myApp", "[#] FragmentGPSFix MEASURED: " + layoutHeight + " / " + 3.9*viewHeight + " -> " + isTimeAndSatellitesVisible);
            }
            GPSApplication.getInstance().setSpaceForExtraTilesAvailable(isTimeAndSatellitesVisible);
            try {
                update();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_team_details, container, false);

        // FrameLayouts
        flTeamDetails = view.findViewById(R.id.id_fragmentTeamDetailsFrameLayout);

        flCrewNameText = view.findViewById(R.id.id_crew_name);

        flTeamDivisionDropDown = view.findViewById(R.id.id_division_spinner);
        ArrayList<String> divisions = teamDetailsManager.getDivisionList();
        dataAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, divisions);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flTeamDivisionDropDown.setAdapter(dataAdapter);

        Button flSaveButton = view.findViewById(R.id.id_team_detail_save_button);
        flSaveButton.setOnClickListener(v -> EventBus.getDefault().post(EventBusMSG.UPDATE_TEAM_DATA));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Workaround for Nokia Devices, Android 9
        // https://github.com/BasicAirData/GPSLogger/issues/77
        if (EventBus.getDefault().isRegistered(this)) {
            //Log.w("myApp", "[#] FragmentGPSFix.java - EventBus: FragmentGPSFix already registered");
            EventBus.getDefault().unregister(this);
        }

        EventBus.getDefault().register(this);

        ViewTreeObserver vto = flTeamDetails.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(viewTreeObserverOnGLL);

        try {
            update();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            flTeamDetails.getViewTreeObserver().removeGlobalOnLayoutListener(viewTreeObserverOnGLL);
        } else {
            flTeamDetails.getViewTreeObserver().removeOnGlobalLayoutListener(viewTreeObserverOnGLL);
        }

        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * The EventBus receiver for Short Messages.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Short msg) throws ExecutionException, InterruptedException, MalformedURLException {
        if (msg == EventBusMSG.UPDATE_TEAM_DATA) {
            update();
        }
    }

    /**
     * Updates the user interface of the fragment.
     * It takes care of visibility and value of each tile, messages, and GPS Status widgets.
     */

    public void update() throws MalformedURLException, ExecutionException, InterruptedException {
        this.updateTeamData();
        this.updateDivisionData();
        EventBus.getDefault().post(EventBusMSG.UPDATE_TRACK);
    }

    private void updateTeamData() throws ExecutionException, InterruptedException {
        String crewName = flCrewNameText.getText().toString();
        String division = flTeamDivisionDropDown.getSelectedItem().toString();
        Log.w("myApp", "[#] FragmentTeamDetails.java - Update");

        if(TextUtils.isEmpty(crewName)) {
            flCrewNameText.setError("Crew name cannot be empty");
            return;
        }
        boolean status = this.teamDetailsManager.validateTeamInfo(crewName,division);
        Toast toast;
        if(status) {
            toast = Toast.makeText(getContext(), "Team set. You can now start streaming the data", Toast.LENGTH_LONG);
        } else {
            this.teamDetailsManager.resetTeamDetails();
            this.gpsApplication.setRecording(false);
            toast = Toast.makeText(getContext(), R.string.toast_error_team, Toast.LENGTH_LONG);
        }
        toast.setGravity(Gravity.BOTTOM, 0, TOAST_VERTICAL_OFFSET);
        toast.show();
    }

    private void updateDivisionData() throws ExecutionException, InterruptedException {
        this.teamDetailsManager.updateDivisionList();
        this.dataAdapter.clear();
        this.dataAdapter.addAll(this.teamDetailsManager.getDivisionList());
        Log.w("myApp", "[#] FragmentTeamDetails.java - Update Division Data");
    }
}