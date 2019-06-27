package com.example.ivangarrera.example.Controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Message;

import com.example.ivangarrera.example.Model.GuideManagement;
import com.example.ivangarrera.example.Views.MainActivity;

import java.util.concurrent.Semaphore;

public class ReceiveBTMessageHandler extends android.os.Handler {
    public static Semaphore mutex_timestamp = new Semaphore(1, true);
    public static long prev_timestamp;
    public static long timestamp_temperature_humidity;
    private long timestamp_group_level;
    private final int TIME_GROUP_LEVEL = 60; // in seconds
    private Context context;
    private Activity related_activity;
    private StringBuilder string_builder = new StringBuilder();


    public ReceiveBTMessageHandler(Context context, Activity related_activity) {
        this.context = context;
        this.related_activity = related_activity;
        prev_timestamp = System.currentTimeMillis() / 1000;
        timestamp_temperature_humidity = System.currentTimeMillis() / 1000;
        timestamp_group_level = System.currentTimeMillis() / 1000;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        final int RECEIVE_MESSAGE = 1;

        switch (msg.what) {
            case RECEIVE_MESSAGE:
                // msg.obj contains the length of the incoming buffer
                byte[] incoming_buffer = (byte[]) msg.obj;
                // msg.arg1 contains the bytes
                String incoming_string = new String(incoming_buffer, 0, msg.arg1);
                string_builder.append(incoming_string);
                int endOfLineIndex = string_builder.indexOf("\r\n");

                if (endOfLineIndex == 0) {
                    string_builder.delete(0, string_builder.length());
                }

                if (endOfLineIndex > 0) {
                    // line variable contains the full packaged data
                    String line = string_builder.substring(0, endOfLineIndex);

                    // Create a new thread to detect falls
                    new Thread(new FallDetectorRunnable().init(line, context, related_activity)).start();

                    // Create a new thread to process the received data (temperature, risk analysis, etc.)
                    new Thread(new ProcessMessageRunnable().init(line, context, related_activity)).start();
                    string_builder.delete(0, string_builder.length());

                    // Update grouping level stuff ?
                    if (((System.currentTimeMillis() / 1000) - timestamp_group_level) >= TIME_GROUP_LEVEL
                            && MainActivity.expedition_name != null) {
                        GuideManagementController guideManagement = new GuideManagementController(null);
                        guideManagement.setGroupingLevel(MainActivity.expedition_name, related_activity);
                        timestamp_group_level = System.currentTimeMillis() / 1000;
                    }
                }
                break;
        }
    }
}
