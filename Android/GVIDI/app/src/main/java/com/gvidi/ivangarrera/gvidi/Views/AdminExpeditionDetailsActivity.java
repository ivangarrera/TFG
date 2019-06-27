package com.example.ivangarrera.example.Views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.example.ivangarrera.example.Controller.ExpeditionManagementController;
import com.example.ivangarrera.example.Model.ExpeditionManagement;
import com.example.ivangarrera.example.R;

public class AdminExpeditionDetailsActivity extends AppCompatActivity {
    private static final String TAG = "GVIDI";
    private NumberPicker numberPicker, hourPicker, distancePicker, distancePickerValue, batteryLevel;
    private Button btn_make_stop, btn_repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_expedition_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        numberPicker = findViewById(R.id.picker_alerts);
        hourPicker = findViewById(R.id.picker_hour);
        distancePicker = findViewById(R.id.picker_meters);
        distancePickerValue = findViewById(R.id.picker_meters_string);
        batteryLevel = findViewById(R.id.picker_battery_level);
        btn_make_stop = findViewById(R.id.btn_stop);
        btn_repeat = findViewById(R.id.btn_repeat);

        configurePickers();

        final ExpeditionManagementController expeditionManagement = new ExpeditionManagementController();
        if (MainActivity.expedition_name != null) {
            // By default, show the last five minutes
            expeditionManagement.fillAllParticipantAlerts(MainActivity.expedition_name, this, 300);
            // By default, show participants separated more than 450 meters
            expeditionManagement.fillParticipantsMoreDistance(MainActivity.expedition_name, this, 450);
            expeditionManagement.fillExpeditionStops(MainActivity.expedition_name, this);
            // By default, the battery level is 50%
            expeditionManagement.fillExpeditionBattery(MainActivity.expedition_name, this, 50);
        }

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int amount_of_seconds = (newVal - 1) * 60;
                // Check if the interval is given in hours
                if (hourPicker.getValue() == 0) {
                    amount_of_seconds *= 60;
                }

                expeditionManagement.fillAllParticipantAlerts(MainActivity.expedition_name,
                        AdminExpeditionDetailsActivity.this, amount_of_seconds);
            }
        });

        hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int amount_of_seconds = (numberPicker.getValue() - 1) * 60;

                // If the interval is given in hours
                if (newVal == 0) {
                    amount_of_seconds *= 60;
                }

                expeditionManagement.fillAllParticipantAlerts(MainActivity.expedition_name,
                        AdminExpeditionDetailsActivity.this, amount_of_seconds);
            }
        });

        distancePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int distance_in_meters = (newVal - 1) * 50;
                expeditionManagement.fillParticipantsMoreDistance(MainActivity.expedition_name,
                        AdminExpeditionDetailsActivity.this, distance_in_meters);
            }
        });

        batteryLevel.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int battery_level = newVal - 1;
                Log.d(TAG, String.valueOf(battery_level));
                expeditionManagement.fillExpeditionBattery(MainActivity.expedition_name,
                        AdminExpeditionDetailsActivity.this, battery_level);
            }
        });

        btn_make_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset the last stop timestamp
                expeditionManagement.updateLastStop(MainActivity.expedition_name);
                expeditionManagement.fillExpeditionStops(MainActivity.expedition_name,
                        AdminExpeditionDetailsActivity.this);
            }
        });

        btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.expedition_name != null) {
                    hourPicker.setValue(1);
                    distancePickerValue.setValue(0);
                    numberPicker.setValue(6);
                    batteryLevel.setValue(51);
                    distancePicker.setValue(10);

                    // By default, show the last five minutes
                    expeditionManagement.fillAllParticipantAlerts(MainActivity.expedition_name,
                            AdminExpeditionDetailsActivity.this, 300);
                    // By default, show participants separated more than 450 meters
                    expeditionManagement.fillParticipantsMoreDistance(MainActivity.expedition_name,
                            AdminExpeditionDetailsActivity.this, 450);
                    expeditionManagement.fillExpeditionStops(MainActivity.expedition_name,
                            AdminExpeditionDetailsActivity.this);
                    expeditionManagement.fillExpeditionBattery(MainActivity.expedition_name,
                            AdminExpeditionDetailsActivity.this, 50);
                }
            }
        });
    }

    private void configurePickers() {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(1);
        hourPicker.setDisplayedValues(new String[]{"Horas", "Minutos"});
        hourPicker.setValue(1);

        distancePickerValue.setMinValue(0);
        distancePickerValue.setMaxValue(0);
        distancePickerValue.setDisplayedValues(new String[]{"Metros"});
        distancePickerValue.setValue(0);

        String[] numbers = new String[60];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.toString(i);
        }

        String[] distances = new String[40];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = Integer.toString(i * 50);
        }

        String[] battery = new String[100];
        for (int i = 0; i < battery.length; i++) {
            battery[i] = Integer.toString(i);
        }

        batteryLevel.setDisplayedValues(battery);
        batteryLevel.setMinValue(1);
        batteryLevel.setMaxValue(battery.length);
        batteryLevel.setWrapSelectorWheel(false);
        batteryLevel.setValue(51);

        numberPicker.setDisplayedValues(numbers);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(numbers.length);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(6);

        distancePicker.setDisplayedValues(distances);
        distancePicker.setMinValue(1);
        distancePicker.setMaxValue(distances.length);
        distancePicker.setWrapSelectorWheel(false);
        distancePicker.setValue(10);
    }

}
