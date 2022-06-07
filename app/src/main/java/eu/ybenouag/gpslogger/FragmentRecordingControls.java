/*
 * FragmentRecordingControls - Java Class for Android
 * Created by G.Capelli on 20/5/2016
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
 *
 */

package eu.ybenouag.gpslogger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * The Fragment that displays and manages the bottom bar.
 */
public class FragmentRecordingControls extends Fragment {

    private TextView tvLockButton;
    private TextView tvRecordButton;
    private TextView tvCrewName;
    private TextView tvDivision;

    final GPSApplication gpsApp = GPSApplication.getInstance();
    private final TeamDetailsManager teamDetailsManager = TeamDetailsManager.getInstance();

    Vibrator vibrator;

    public FragmentRecordingControls() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recording_controls, container, false);

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        tvLockButton = view.findViewById(R.id.id_lock);
        setButtonToNormalState(tvLockButton, R.drawable.ic_lock_24, R.string.lock);
        tvLockButton.setOnClickListener(v -> {
            if (isAdded())
                ((GPSActivity) requireActivity()).onToggleLock();
        });

        tvRecordButton = view.findViewById(R.id.id_record);
        tvRecordButton.setOnClickListener(v -> {
            if (isAdded() && teamDetailsManager.isTeamRegistered())
                ((GPSActivity) requireActivity()).onToggleRecord();
        });

        tvCrewName = view.findViewById(R.id.id_textView_crewName);

        tvDivision = view.findViewById(R.id.id_textView_division);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Workaround for Nokia Devices, Android 9
        // https://github.com/BasicAirData/GPSLogger/issues/77
        if (EventBus.getDefault().isRegistered(this)) {
            //Log.w("myApp", "[#] FragmentRecordingControls - EventBus: FragmentRecordingControls already registered");
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
        Update();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * The EventBus receiver for Short Messages.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.UPDATE_TRACK) {
            Update();
        }
    }

    /**
     * Sets the color of a drawable.
     *
     * @param drawable The Drawable
     * @param color    The new Color to set
     */
    private void setTextViewDrawableColor(Drawable drawable, int color) {
        if (drawable != null) {
            drawable.clearColorFilter();
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
    }

    /**
     * Sets the appearance of a button (TextView + upper compound Drawable) as "Clicked",
     * by setting the specified Drawable and Text and applying the right colours.
     *
     * @param button   The TextView button
     * @param imageId  The resource of the drawable
     * @param stringId The resource of the string
     */
    private void setButtonToClickedState(@NonNull TextView button, int imageId, int stringId) {
        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        if (imageId != 0) button.setCompoundDrawablesWithIntrinsicBounds(0, imageId, 0, 0);
        button.setTextColor(getResources().getColor(R.color.textColorRecControlSecondary_Active));
        if (stringId != 0) button.setText(getString(stringId));
        setTextViewDrawableColor(button.getCompoundDrawables()[1], getResources().getColor(R.color.textColorRecControlPrimary_Active));
    }

    /**
     * Sets the appearance of a button (TextView + upper compound Drawable) as "Normal",
     * by setting the specified Drawable and Text and applying the right colours.
     *
     * @param button   The TextView button
     * @param imageId  The resource of the drawable
     * @param stringId The resource of the string
     */
    private void setButtonToNormalState(@NonNull TextView button, int imageId, int stringId) {
        button.setBackgroundColor(Color.TRANSPARENT);
        if (imageId != 0) button.setCompoundDrawablesWithIntrinsicBounds(0, imageId, 0, 0);
        button.setTextColor(getResources().getColor(R.color.textColorRecControlSecondary));
        if (stringId != 0) button.setText(getString(stringId));
        setTextViewDrawableColor(button.getCompoundDrawables()[1], getResources().getColor(R.color.textColorRecControlPrimary));
    }

    /**
     * Sets the appearance of a button (TextView + upper compound Drawable) as "Disabled"
     * by setting the specified Drawable and Text and applying the right colours.
     *
     * @param button The TextView button
     */
    private void setButtonToDisabledState(@NonNull TextView button) {
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setTextColor(getResources().getColor(R.color.textColorRecControlDisabled));
        setTextViewDrawableColor(button.getCompoundDrawables()[1], getResources().getColor(R.color.textColorRecControlDisabled));
    }

    /**
     * Updates the user interface of the fragment.
     * It takes care of the state of each button.
     */
    public void Update() {
        final boolean isTeamRegistered = teamDetailsManager.isTeamRegistered();
        final boolean isLck = gpsApp.isBottomBarLocked();

        if (isAdded() && isTeamRegistered) {
            final boolean isRec = gpsApp.isRecording();
            if (tvRecordButton != null) {
                if (isRec) {
                    setButtonToClickedState(tvRecordButton, R.drawable.ic_pause_24, R.string.pause);
                } else {
                    setButtonToNormalState(tvRecordButton, R.drawable.ic_record_24, R.string.record);
                }
            }
        } else {
            setButtonToNormalState(tvRecordButton, R.drawable.ic_record_24, R.string.record);
            setButtonToDisabledState(tvRecordButton);
            // setButtonToDisabledState(tvStopButton, 0, 0);
        }

        if (tvLockButton != null) {
            if (isLck)
                setButtonToClickedState(tvLockButton, R.drawable.ic_unlock_24, R.string.unlock);
            else setButtonToNormalState(tvLockButton, R.drawable.ic_lock_24, R.string.lock);
        }

        if (tvDivision != null) {
            tvDivision.setText(teamDetailsManager.getDivision());
        }

        if (tvCrewName != null) {
            tvCrewName.setText(teamDetailsManager.getCrewName());
        }
    }
}