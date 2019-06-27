package com.example.ivangarrera.example.Model;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.example.ivangarrera.example.R;
import com.example.ivangarrera.example.Views.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class GuideManagement {
    private final String DATABASE_NAME = "Expeditions";
    private final String DATABASE_ADMINS = "Admins";
    private FirebaseFirestore database;
    private Semaphore mutex;

    public GuideManagement(Semaphore mutex) {
        this.mutex = mutex;
        database = FirebaseFirestore.getInstance();
    }

    public void addGuide(String expedition_name) {
        Map<String, Object> expedition = new HashMap<>();
        Map<String, Object> guide = new HashMap<>();

        guide.put("id", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        guide.put("Alerts", new ArrayList<Map<String, Object>>());
        guide.put("Temperature", new ArrayList<Map<String, Object>>());
        guide.put("Humidity", new ArrayList<Map<String, Object>>());
        guide.put("Lat", "");
        guide.put("Lon", "");
        guide.put("PreviousLocations", new ArrayList<Map<String, Object>>());
        guide.put("TotalDistance", "0");
        expedition.put("ExpName", expedition_name);
        expedition.put("Guide", guide);

        database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GVIDI", "Guide added correctly");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("GVIDI", "Guide cannot be added");
                    }
                });
    }

    public void checkIfGuide() {
        try {
            mutex.acquire(1);
            DocumentReference documentReference = database.collection(DATABASE_ADMINS).document(DATABASE_ADMINS);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        ArrayList<String> admin_list = (ArrayList<String>) task.getResult().getData().get("Admin_list");
                        for (String admin : admin_list) {
                            if (admin.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                MainActivity.b_canLeaveExpedition = false;
                                MainActivity.b_canJoinExpedition = false;
                                MainActivity.b_canCreateExpedition = true;

                                checkIfGuideIsInExpedition();
                                return;
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            Log.e("GVIDI", ex.getMessage());
        } finally {
            mutex.release(1);
        }
    }

    private void checkIfGuideIsInExpedition() {
        CollectionReference collectionReference = database.collection(DATABASE_NAME);
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    for (DocumentSnapshot document : documents) {
                        Map<String, Object> expedition = document.getData();
                        if ((boolean) expedition.get("InProgress")) {
                            Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");
                            if (guide.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                MainActivity.expedition_name = (String) expedition.get("ExpName");
                                MainActivity.b_canCreateExpedition = false;
                                MainActivity.b_canCancelExpedition = true;
                                return;
                            }
                        }
                    }
                }
            }
        });
    }

    public void addAlert(final String expedition_name, final String latitude, final String longitude, final String alert_type) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");

                    if (guide.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        ArrayList<Map<String, Object>> alerts =
                                (ArrayList<Map<String, Object>>) guide.get("Alerts");

                        Map<String, Object> new_alert = new HashMap<>();
                        new_alert.put("AlertType", alert_type);
                        new_alert.put("Latitude", guide.get("Lat"));
                        new_alert.put("Longitude", guide.get("Lon"));
                        new_alert.put("Timestamp", System.currentTimeMillis() / 1000);

                        alerts.add(new_alert);
                        guide.put("Alerts", alerts);
                        expedition.put("Guide", guide);
                        database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                    }
                }
            }
        });
    }

    public void addTemperatureAndHumidity(final String expedition_name, final double temperature, final double humidity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");
                    long timestamp = System.currentTimeMillis() / 1000;

                    ArrayList<Map<String, Object>> temperatures =
                            (ArrayList<Map<String, Object>>) guide.get("Temperature");
                    Map<String, Object> my_temp = new HashMap<>();
                    my_temp.put("Data", temperature);
                    my_temp.put("Timestamp", timestamp);
                    temperatures.add(my_temp);

                    ArrayList<Map<String, Object>> humidities =
                            (ArrayList<Map<String, Object>>) guide.get("Humidity");
                    Map<String, Object> my_humi = new HashMap<>();
                    my_humi.put("Data", humidity);
                    my_humi.put("Timestamp", timestamp);
                    humidities.add(my_humi);

                    guide.put("Temperature", temperatures);
                    guide.put("Humidity", humidities);
                    expedition.put("Guide", guide);
                    database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                }
            }
        });
    }

    public void setGroupingLevel(final String expedition_name, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    double radius_grouping_level = 0.f;
                    try {
                        double guide_lat = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lat").toString())));
                        double guide_lon = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lon").toString())));

                        for (Map<String, Object> participant : participants) {
                            double part_lat = Math.toRadians(Math.abs(Double.parseDouble(
                                    participant.get("Lat").toString())));
                            double part_lon = Math.toRadians(Math.abs(Double.parseDouble(
                                    participant.get("Lon").toString())));
                            radius_grouping_level += ParticipantManagement.getDistanceBetweenTwoPoints(
                                    guide_lat, guide_lon, part_lat, part_lon);
                        }
                        if (participants.size() > 0) {
                            radius_grouping_level /= participants.size();
                            TextView lbl_grouping_level = activity.findViewById(R.id.label_group_level_value);
                            String unit;
                            if (radius_grouping_level < 1) {
                                radius_grouping_level *= 1000;
                                unit = activity.getResources().getString(R.string.str_meters);
                            } else {
                                unit = activity.getResources().getString(R.string.str_km);
                            }
                            lbl_grouping_level.setText(String.format(Locale.UK, "%.2f %s", radius_grouping_level, unit));
                        }
                    } catch (Exception ex) {
                        Log.e("TAG", ex.getMessage());
                    }
                }
            }

        });
    }

    public void updateBattery(final String expedition_name, final int battery_level) {
        if (expedition_name != null) {
            DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Map<String, Object> expedition = task.getResult().getData();
                        Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");

                        try {
                            if (guide.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                guide.put("BatteryLevel", battery_level);
                            }
                        } catch (NullPointerException ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }

                        expedition.put("Guide", guide);
                        database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                    }
                }
            });
        }
    }

    public void updatePosition(final String expedition_name, final String latitude, final String longitude) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");

                    try {
                        if (guide.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            // Update the previousLocations array, pushing the coordinates that are
                            // going to be updated
                            ArrayList<Map<String, Object>> previousLocations =
                                    (ArrayList<Map<String, Object>>) guide.get("PreviousLocations");
                            Map<String, Object> prev_loc = new HashMap<>();
                            prev_loc.put("Lat", guide.get("Lat"));
                            prev_loc.put("Lon", guide.get("Lon"));
                            prev_loc.put("Timestamp", System.currentTimeMillis() / 1000);
                            previousLocations.add(prev_loc);

                            // Update the current coordinates by the new ones
                            guide.put("PreviousLocations", previousLocations);
                            guide.put("Lat", latitude);
                            guide.put("Lon", longitude);

                            //lbl_coordinates.setText("(" + latitude + ", " + longitude + ")");

                            expedition.put("Guide", guide);
                            database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                        }
                    } catch (NullPointerException ex) {
                        Log.e("GVIDI", ex.getMessage());
                    }

                    updateGuideTotalDistance(expedition_name);
                }
            }
        });
    }

    public void calculateInstantaneousPace(final String expedition_name, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");


                    ArrayList<Map<String, Object>> previousLocations =
                            (ArrayList<Map<String, Object>>) guide.get("PreviousLocations");

                    // Get the coordinates and the timestamp of the last element
                    // in the previousLocations array
                    try {
                        double prev_lat = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                get(previousLocations.size() - 1).get("Lat").toString())));
                        double prev_lon = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                get(previousLocations.size() - 1).get("Lon").toString())));
                        long prev_timestamp = (long) previousLocations.get(previousLocations
                                .size() - 1).get("Timestamp");

                        // Get the current location
                        double current_lat = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lat").toString())));
                        double current_lon = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lon").toString())));
                        long current_timestamp = System.currentTimeMillis() / 1000;

                        // Get the time elapsed between two location updates (in minutes)
                        double delta_time = (current_timestamp - prev_timestamp) / 60.0;
                        // Get the distance (in kms) between two points
                        double distance = ParticipantManagement.getDistanceBetweenTwoPoints(prev_lat, prev_lon, current_lat, current_lon);

                        double pace = delta_time / distance;
                        TextView lbl_pace = activity.findViewById(R.id.label_pace_value);

                        if (pace > 100 || pace == 0) {
                            lbl_pace.setText(String.format("%s", activity.getResources().getString(R.string.str_stopped)));
                        } else {
                            lbl_pace.setText(String.format(Locale.UK, "%.2f %s", pace,
                                    activity.getResources().getString(R.string.str_pace)));
                        }
                    } catch (Exception ex) {
                        Log.e("GVIDI", ex.getMessage());

                    }
                }
            }
        });
    }

    public void updateGuideTotalDistance(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");

                    try {
                        if (guide.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            ArrayList<Map<String, Object>> previousLocations =
                                    (ArrayList<Map<String, Object>>) guide.get("PreviousLocations");

                            // Get the coordinates of the last element in the previousLocations array
                            double prev_lat = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                    get(previousLocations.size() - 1).get("Lat").toString())));
                            double prev_lon = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                    get(previousLocations.size() - 1).get("Lon").toString())));

                            // Get the current location
                            double current_lat = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lat").toString())));
                            double current_lon = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lon").toString())));

                            double distance = ParticipantManagement.getDistanceBetweenTwoPoints(
                                    prev_lat, prev_lon, current_lat, current_lon);
                            // Update the total distance of the guide
                            guide.put("TotalDistance", distance +
                                    Double.parseDouble(guide.get("TotalDistance").toString()));

                            expedition.put("Guide", guide);
                            database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                        }
                    } catch (Exception ex) {
                        Log.e("GVIDI", ex.getMessage());
                    }

                }
            }
        });
    }

}
