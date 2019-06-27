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
import java.util.HashMap;
import java.util.Map;

public class FallDetectorRunnable implements Runnable {
    private static final String TAG = "GVIDI";
    private static final double G = 9.8;
    private static final double RAW_ACCEL_TO_SI = 0.00059814453125; // 9.8 / 16384
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

    private double getAccelForce(String data) {
        String[] lines = data.split("\\r?\\n");
        Map acceleration = new HashMap();
        for (int i = 0; i < 3; i++) {
            String[] word = lines[i].split(":");
            try {
                acceleration.put(word[0], Double.parseDouble(word[1]));
            } catch (NumberFormatException ex) {
                return -1000;
            }
        }

        return Math.sqrt(Math.pow((double) acceleration.get("ax"), 2) +
                Math.pow((double) acceleration.get("ay"), 2) +
                Math.pow((double) acceleration.get("az"), 2)) * RAW_ACCEL_TO_SI;
    }

    private double getEulerAngle(String data) {
        String[] lines = data.split("\\r?\\n");
        Map acceleration = new HashMap();
        for (int i = 0; i < 3; i++) {
            String[] word = lines[i].split(":");
            try {
                acceleration.put(word[0], Double.parseDouble(word[1]));
            } catch (NumberFormatException ex) {
                return -1000;
            }
        }
        return Math.atan(Math.sqrt(Math.pow((double) acceleration.get("ay"), 2) +
                Math.pow((double) acceleration.get("az"), 2)) /
                (double) acceleration.get("ax")) * (180 / Math.PI);
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
            final ParticipantManagementController participantManagement = new ParticipantManagementController(null);
            GuideManagementController guideManagement = new GuideManagementController(null);

            // Calculate the current force from the accelerometer and calculate the euler angle too
            // With this two values, we can guess if a fall has occurred
            final double force = getAccelForce(thread_line);
            final double euler_angle = getEulerAngle(thread_line);
            if (force != -1000 && euler_angle != -1000) {

                Log.e(TAG, String.format("%.3f -> %.3f", force, euler_angle));
                // Check if a fall has occurred
                FD_Event event_occurred;
                if (force < 0.65 * G) {
                    event_occurred = FD_Event.FD_DataLessThreshold;
                } else if (force > 2.5 * G) {
                    event_occurred = FD_Event.FD_DataMoreThreshold;
                } else {
                    event_occurred = FD_Event.FD_DataInsideThreshold;
                }

                FallDetector fallDetector = FallDetector.getInstance();
                fallDetector.makeTransition(event_occurred, force);

                boolean b_isSOS = false;

                // Add SOS alert. A SOS alert has occurred if our FallDetector algorithm has found
                // a behaviour considered as a fall
                if (fallDetector.getMyState().equals(FD_State.FD_FALL)) {
                    if (MainActivity.b_canCreateExpedition || MainActivity.b_canCancelExpedition) {
                        guideManagement.addAlert(MainActivity.expedition_name, null, null, "SOS");
                    } else if (MainActivity.b_canJoinExpedition || MainActivity.b_canLeaveExpedition) {
                        participantManagement.addAlert(MainActivity.expedition_name, null, null, "SOS");
                    }
                    b_isSOS = true;
                }
                final boolean b_isSOS_def = b_isSOS;

                // Update GUI
                related_activity.runOnUiThread(new Runnable() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void run() {
                        if (b_isSOS_def) {
                            ImageView img_sos = related_activity.findViewById(R.id.circle_sos);
                            img_sos.setBackground(context.getDrawable(R.drawable.round_shape));
                            img_sos.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.redColor)));
                        }
                    }
                });
                Log.d(TAG, fallDetector.getMyState().toString());
            }
        }
    }
}
