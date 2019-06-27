package com.example.ivangarrera.example.Controller;

import android.app.Activity;

import com.example.ivangarrera.example.Model.ExpeditionManagement;
import com.google.android.gms.maps.GoogleMap;

public class ExpeditionManagementController {
    private ExpeditionManagement expeditionManagement;

    public ExpeditionManagementController() {
        expeditionManagement = new ExpeditionManagement();
    }

    public void addExpedition(String expeditionName) {
        expeditionManagement.addExpedition(expeditionName);
    }

    public void closeExpedition(String expeditionName) {
        expeditionManagement.closeExpedition(expeditionName);
    }

    public void fillExpeditionsToJoin(Activity activity) {
        expeditionManagement.fillExpeditionsToJoin(activity);
    }

    public void fillAllParticipantAlerts(String expeditionName, Activity activity, int interval_in_seconds) {
        expeditionManagement.fillAllParticipantAlerts(expeditionName, activity, interval_in_seconds);
    }

    public void fillParticipantsMoreDistance(String expedition_name, Activity activity, int maximum_distance) {
        expeditionManagement.fillParticipantsMoreDistance(expedition_name, activity, maximum_distance);
    }

    public void fillExpeditionStops(String expedition_name, Activity activity) {
        expeditionManagement.fillExpeditionStops(expedition_name, activity);
    }

    public void fillExpeditionBattery(String expedition_name, Activity activity, int battery_level) {
        expeditionManagement.fillExpeditionBattery(expedition_name, activity, battery_level);
    }

    public void addDistanceAlert(String expedition_name, String other_participant_id) {
        expeditionManagement.addDistanceAlert(expedition_name, other_participant_id);
    }

    public void updateLastStop(String expedition_name) {
        expeditionManagement.updateLastStop(expedition_name);
    }

    public void updateMap(String expedition_name, GoogleMap mMap) {
        expeditionManagement.updateMap(expedition_name, mMap);
    }

}