package com.example.ivangarrera.example.Controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivangarrera.example.R;
import com.example.ivangarrera.example.Views.MainActivity;

import java.util.Arrays;

public class ProcessMessageRunnable implements Runnable {
    private static final String TAG = "GVIDI";
    private static final int TIME_CHANGE_PARAMETERS_FROM_FIREBASE = 15; // in seconds
    private static final int TIME_UPDATE_TEMP_HUMIDITY = 300; // Five minutes (300 seconds)
    private static final int TIME_RISK_ANALYSIS = 10; // One minute (60 seconds)
    private String thread_line;
    private Context context;
    private Activity related_activity;

    private boolean checkCorrectFormat(String data) {
        String header_format[] = {"ax", "ay", "az", "gx", "gy", "gz", "Alert", "Stop",
                "Temperature", "Humidity", "lat", "lon", "Light", "Rain"};

        String[] lines = data.split("\\r?\\n");

        // Check if the length of the received packet is correct
        if (lines.length == header_format.length) {
            for (int index = 0; index < lines.length; index++) {
                String[] word = lines[index].split(":");

                if (!header_format[index].equals(word[0])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private String getValueFromData(String[] lines, String sensor_index) {
        String header_format[] = {"ax", "ay", "az", "gx", "gy", "gz", "Alert", "Stop",
                "Temperature", "Humidity", "lat", "lon", "Light", "Rain"};
        try {
            return lines[Arrays.asList(header_format).indexOf(sensor_index)].split(":")[1];
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return "";
        }
    }

    public Runnable init(String line, Context context, Activity related_activity) {
        thread_line = line;
        this.context = context;
        this.related_activity = related_activity;
        return this;
    }

    @Override
    public void run() {
        if (checkCorrectFormat(thread_line)) {
            String[] lines = thread_line.split("\\r?\\n");

            final ParticipantManagementController participantManagement = new ParticipantManagementController(null);
            GuideManagementController guideManagement = new GuideManagementController(null);

            final String latitude = getValueFromData(lines, "lat").replace(" ", "");
            final String longitude = getValueFromData(lines, "lon").replace(" ", "");

            final boolean b_isSOS = getValueFromData(lines, "Alert").equals(" true");
            final boolean b_isStop = getValueFromData(lines, "Stop").equals(" true");

            // Add SOS alert. A SOS alert has occurred if the participant has pulsed the SOS button
            if (b_isSOS) {
                if (MainActivity.b_canCreateExpedition || MainActivity.b_canCancelExpedition) {
                    guideManagement.addAlert(MainActivity.expedition_name, latitude, longitude, "SOS");
                } else if (MainActivity.b_canJoinExpedition || MainActivity.b_canLeaveExpedition) {
                    participantManagement.addAlert(MainActivity.expedition_name, latitude, longitude, "SOS");
                }
            }

            // Add STOP alert
            if (b_isStop) {
                if (MainActivity.b_canCreateExpedition || MainActivity.b_canCancelExpedition) {
                    guideManagement.addAlert(MainActivity.expedition_name, latitude, longitude, "STOP");
                } else if (MainActivity.b_canJoinExpedition || MainActivity.b_canLeaveExpedition) {
                    participantManagement.addAlert(MainActivity.expedition_name, latitude, longitude, "STOP");
                }
            }

            // Get the rest of the parameters (temperature, humidity, rain, etc.)
            final String temperature = getValueFromData(lines, "Temperature").replace(" ", "");
            final String humidity = getValueFromData(lines, "Humidity").replace(" ", "");
            final String light = getValueFromData(lines, "Light").replace(" ", "");
            final String rain = getValueFromData(lines, "Rain").replace(" ", "");

            // Perform a risk analysis each 2 minutes
            int low = -1, mid = -1, high = -1;

            long delta = System.currentTimeMillis() / 1000 - ReceiveBTMessageHandler.timestamp_temperature_humidity;
            if (delta >= TIME_RISK_ANALYSIS) {
                FuzzyRiskAnalysis.Build();
                String risk = FuzzyRiskAnalysis.CalculateRisk(Double.parseDouble(humidity), Double.parseDouble(rain), Double.parseDouble(light));
                if (risk != null) {
                    String[] s = risk.split("The risk is: ");
                    s = s[0].split(",");

                    try {
                        low = Integer.parseInt(s[1].split("/")[0]);
                        mid = Integer.parseInt(s[2].split("/")[0]);
                        high = Integer.parseInt(s[3].split("/")[0]);
                        if (mid == 0 && high == 0) {
                            low = 1;
                        }
                    } catch (Exception ex) {
                        Log.e("GVIDI", ex.getMessage());
                    }
                } else {
                    Log.e("GVIDI", "Risk is null");
                }
                ReceiveBTMessageHandler.timestamp_temperature_humidity = System.currentTimeMillis() / 1000;
            }

            // Add temperature and humidity values to Firestore
            delta = (System.currentTimeMillis() / 1000) - ReceiveBTMessageHandler.timestamp_temperature_humidity;
            try {
                if (delta >= TIME_UPDATE_TEMP_HUMIDITY) {
                    if (MainActivity.expedition_name != null && !temperature.equals("null") && !humidity.equals("null")) {
                        if (MainActivity.b_canCreateExpedition || MainActivity.b_canCancelExpedition) {
                            guideManagement.addTemperatureAndHumidity(MainActivity.expedition_name,
                                    Double.parseDouble(temperature), Double.parseDouble(humidity));
                        } else if (MainActivity.b_canJoinExpedition || MainActivity.b_canLeaveExpedition) {
                            participantManagement.addTemperatureAndHumidity(MainActivity.expedition_name,
                                    Double.parseDouble(temperature), Double.parseDouble(humidity));
                        }
                        ReceiveBTMessageHandler.timestamp_temperature_humidity = System.currentTimeMillis() / 1000;
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

            final int low_risk = low;
            final int mid_risk = mid;
            final int high_risk = high;

            // Update GUI
            related_activity.runOnUiThread(new Runnable() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void run() {
                    if (b_isSOS) {
                        ImageView img_sos = related_activity.findViewById(R.id.circle_sos);
                        img_sos.setBackground(context.getDrawable(R.drawable.round_shape));
                        img_sos.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.redColor)));
                    }
                    if (b_isStop) {
                        ImageView img_stop = related_activity.findViewById(R.id.circle_stop);
                        img_stop.setBackground(context.getDrawable(R.drawable.round_shape));
                        img_stop.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.redColor)));
                    }

                    TextView lbl_temperature = related_activity.findViewById(R.id.label_temperature_value);
                    TextView lbl_humidity = related_activity.findViewById(R.id.label_humidity_value);
                    TextView lbl_rain = related_activity.findViewById(R.id.label_rain_value);

                    lbl_temperature.setText(String.format("%s ºC", temperature));
                    lbl_humidity.setText(String.format("%s %s", humidity,
                            related_activity.getResources().getString(R.string.percent)));
                    try {
                        lbl_rain.setText(Double.parseDouble(rain) < 500 ? "SI" : "NO");
                    } catch (NumberFormatException ex) {
                        Log.e(TAG, ex.getMessage());
                    }

                    // Set the expedition risk
                    TextView lbl_risk = related_activity.findViewById(R.id.label_risk_value);
                    if (high_risk != -1 && mid_risk != -1 && low_risk != -1) {
                        if (high_risk >= mid_risk && high_risk >= low_risk) { // the risk is high
                            lbl_risk.setText(related_activity.getResources().getString(R.string.label_high_risk));
                        } else if (mid_risk >= high_risk && mid_risk >= low_risk) { // the risk is mid
                            lbl_risk.setText(related_activity.getResources().getString(R.string.label_mid_risk));
                        } else if (low_risk >= high_risk && low_risk >= mid_risk) { // the risk is low
                            lbl_risk.setText(related_activity.getResources().getString(R.string.label_low_risk));
                        } else {
                            lbl_risk.setText(related_activity.getResources().getString(R.string.calculating));
                        }
                    }

                    // Modify the grouping stuff and the total distance
                    try {
                        ReceiveBTMessageHandler.mutex_timestamp.acquire(1);
                        if ((System.currentTimeMillis() / 1000) - ReceiveBTMessageHandler.prev_timestamp >= TIME_CHANGE_PARAMETERS_FROM_FIREBASE) {
                            if (MainActivity.expedition_name != null) {
                                participantManagement.fillMainGUI(MainActivity.expedition_name, related_activity);
                            }
                            ReceiveBTMessageHandler.prev_timestamp = System.currentTimeMillis() / 1000;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    } finally {
                        ReceiveBTMessageHandler.mutex_timestamp.release(1);
                    }
                }
            });
        }
    }
}
