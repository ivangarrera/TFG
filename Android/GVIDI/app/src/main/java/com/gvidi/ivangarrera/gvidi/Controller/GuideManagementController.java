package com.example.ivangarrera.example.Controller;

import android.app.Activity;

import com.example.ivangarrera.example.Model.GuideManagement;

import java.util.concurrent.Semaphore;

public class GuideManagementController {
    private GuideManagement guideManagement;

    public GuideManagementController(Semaphore mutex) {
        guideManagement = new GuideManagement(mutex);
    }

    public void addGuide(String expedition_name) {
        guideManagement.addGuide(expedition_name);
    }

    public void checkIfGuide() {
        guideManagement.checkIfGuide();
    }

    public void addAlert(String expedition_name, String latitude, String longitude, String alert_type) {
        guideManagement.addAlert(expedition_name, latitude, longitude, alert_type);
    }

    public void addTemperatureAndHumidity(String expedition_name, double temperature, double humidity) {
        guideManagement.addTemperatureAndHumidity(expedition_name, temperature, humidity);
    }

    public void setGroupingLevel(String expedition_name, Activity activity) {
        guideManagement.setGroupingLevel(expedition_name, activity);
    }

    public void updateBattery(String expedition_name, int battery_level) {
        guideManagement.updateBattery(expedition_name, battery_level);
    }

    public void updatePosition(String expedition_name, String latitude, String longitude) {
        guideManagement.updatePosition(expedition_name, latitude, longitude);
    }

    public void calculateInstantaneousPace(String expedition_name, Activity activity) {
        guideManagement.calculateInstantaneousPace(expedition_name, activity);
    }

    public void updateGuideTotalDistance(String expedition_name) {
        guideManagement.updateGuideTotalDistance(expedition_name);
    }
}
