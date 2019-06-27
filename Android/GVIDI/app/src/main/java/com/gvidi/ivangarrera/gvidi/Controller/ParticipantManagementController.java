package com.example.ivangarrera.example.Controller;

import android.app.Activity;

import com.example.ivangarrera.example.Model.ParticipantManagement;

import java.util.concurrent.Semaphore;

public class ParticipantManagementController {
    private ParticipantManagement participantManagement;

    public ParticipantManagementController(Semaphore mutex) {
        participantManagement = new ParticipantManagement(mutex);
    }

    public void addParticipant(String expedition_name) {
        participantManagement.addParticipant(expedition_name);
    }

    public void addTemperatureAndHumidity(String expedition_name, double temperature, double humidity) {
        participantManagement.addTemperatureAndHumidity(expedition_name, temperature, humidity);
    }

    public void removeParticipant(String expedition_name) {
        participantManagement.removeParticipant(expedition_name);
    }

    public void checkIfParticipantIsJoined() {
        participantManagement.checkIfParticipantIsJoined();
    }

    public void addAlert(String expedition_name, String latitude, String longitude, String alert_type) {
        participantManagement.addAlert(expedition_name, latitude, longitude, alert_type);
    }

    public void fillParticipantAlerts(String expedition_name, String participant_email, Activity activity) {
        participantManagement.fillParticipantAlerts(expedition_name, participant_email, activity);
    }

    public void fillParticipantDetails(String expedition_name, String participant_email, Activity activity) {
        participantManagement.fillParticipantDetails(expedition_name, participant_email, activity);
    }

    public void fillMainGUI(String expedition_name, Activity activity) {
        participantManagement.fillMainGUI(expedition_name, activity);
    }

    public void updateBattery(String expedition_name, int battery_level) {
        participantManagement.updateBattery(expedition_name, battery_level);
    }

    public void calculateInstantaneousPace(String expedition_name, Activity activity) {
        participantManagement.calculateInstantaneousPace(expedition_name, activity);
    }

    public void updatePosition(String expedition_name, String latitude, String longitude) {
        participantManagement.updatePosition(expedition_name, latitude, longitude);
    }

    public void updateParticipantTotalDistance(String expedition_name) {
        participantManagement.updateParticipantTotalDistance(expedition_name);
    }
}
