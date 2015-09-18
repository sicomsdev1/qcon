package com.sicoms.smartplug.test.timepicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicoms.smartplug.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.adapters.AbstractWheelTextAdapter;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class TimePickerFragment extends Fragment {


    public static TimePickerFragment newInstance() {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_picker, container, false);

        // set current time
        Calendar calendar = Calendar.getInstance(Locale.US);

//        final AbstractWheel day = (AbstractWheel) view.findViewById(R.id.day);
//        DayArrayAdapter dayAdapter = new DayArrayAdapter(getActivity(), calendar);
//        day.setViewAdapter(dayAdapter);
//        day.setCurrentItem(dayAdapter.getToday());

        return view;
    }

//    private class DayArrayAdapter extends AbstractWheelTextAdapter {
//        // Count of days to be shown
//        private final int daysCount = 4;
//
//        // Calendar
//        Calendar calendar;
//
//        /**
//         * Constructor
//         */
//        protected DayArrayAdapter(Context context, Calendar calendar) {
//            super(context, R.layout.adapter_auth, NO_RESOURCE);
//            this.calendar = calendar;
//
//            setItemTextResource(R.id.time2_monthday);
//        }
//        public int getToday() {
//            return daysCount / 2;
//        }
//
//        @Override
//        public View getItem(int index, View cachedView, ViewGroup parent) {
//            int day = -daysCount/2 + index;
//            Calendar newCalendar = (Calendar) calendar.clone();
//            newCalendar.roll(Calendar.DAY_OF_YEAR, day);
//
//            View view = super.getItem(index, cachedView, parent);
//
//            TextView weekday = (TextView) view.findViewById(R.id.time2_weekday);
//            if (day == 0) {
//                weekday.setText("");
//            } else {
//                DateFormat format = new SimpleDateFormat("EEE");
//                weekday.setText(format.format(newCalendar.getTime()));
//            }
//
//            TextView monthday = (TextView) view.findViewById(R.id.time2_monthday);
//            if (day == 0) {
//                monthday.setText("Today");
//                monthday.setTextColor(0xFF0000F0);
//            } else {
//                DateFormat format = new SimpleDateFormat("MMM d");
//                monthday.setText(format.format(newCalendar.getTime()));
//                monthday.setTextColor(0xFF111111);
//            }
//            DateFormat dFormat = new SimpleDateFormat("MMM d");
//            view.setTag(dFormat.format(newCalendar.getTime()));
//            return view;
//        }
//
//        @Override
//        public int getItemsCount() {
//            return daysCount + 1;
//        }
//
//        @Override
//        protected CharSequence getItemText(int index) {
//            return "";
//        }
//    }
}
