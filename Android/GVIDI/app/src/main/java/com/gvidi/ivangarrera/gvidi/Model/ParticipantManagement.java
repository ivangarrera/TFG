package com.example.ivangarrera.example.Model;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivangarrera.example.Controller.AlertListAdapter;
import com.example.ivangarrera.example.Controller.AlertListViewItem;
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

public class ParticipantManagement {
    private final String DATABASE_NAME = "Expeditions";
    private FirebaseFirestore database;
    private Semaphore mutex;

    public ParticipantManagement(Semaphore mutex) {
        this.mutex = mutex;
        database = FirebaseFirestore.getInstance();
    }

    public static double getDistanceBetweenTwoPoints(double latitude_first, double longitude_first,
                                                     double latitude_second, double longitude_second) {
        // Get distance using Haversine formula
        double lat_distance = latitude_first - latitude_second;
        double lon_distance = longitude_first - longitude_second;

        double a = Math.pow(Math.sin(lat_distance / 2), 2) + Math.cos(latitude_second) *
                Math.cos(latitude_first) * Math.pow(Math.sin(lon_distance / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // The 6373.0 value is the radius of the earth
        return 6373.0 * c;
    }

    public void addParticipant(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> participant = new HashMap<>();
                    boolean is_participant_in_expedition = false;

                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");
                    for (Map<String, Object> p : participants) {
                        if (p.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            is_participant_in_expedition = true;
                            p.put("Joined", true);
                            break;
                        }
                    }

                    if (!is_participant_in_expedition) {
                        participant.put("id", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        participant.put("Alerts", new ArrayList<Map<String, Object>>());
                        participant.put("PreviousLocations", new ArrayList<Map<String, Object>>());
                        participant.put("Temperature", new ArrayList<Map<String, Object>>());
                        participant.put("Humidity", new ArrayList<Map<String, Object>>());
                        participant.put("Lat", "");
                        participant.put("Lon", "");
                        participant.put("TotalDistance", "0");
                        participant.put("InitTime", System.currentTimeMillis() / 1000);
                        participant.put("Joined", true);
                        participants.add(participant);
                    }

                    expedition.put("Participants", participants);
                    database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("GVIDI", "Participant added correctly");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("GVIDI", "Participant cannot be added");
                                }
                            });
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
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    for (Map<String, Object> participant : participants) {
                        if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            long timestamp = System.currentTimeMillis() / 1000;

                            ArrayList<Map<String, Object>> temperatures =
                                    (ArrayList<Map<String, Object>>) participant.get("Temperature");
                            Map<String, Object> my_temp = new HashMap<>();
                            my_temp.put("Data", temperature);
                            my_temp.put("Timestamp", timestamp);
                            temperatures.add(my_temp);

                            ArrayList<Map<String, Object>> humidities =
                                    (ArrayList<Map<String, Object>>) participant.get("Humidity");
                            Map<String, Object> my_humi = new HashMap<>();
                            my_humi.put("Data", humidity);
                            my_humi.put("Timestamp", timestamp);
                            humidities.add(my_humi);

                            participant.put("Temperature", temperatures);
                            participant.put("Humidity", humidities);
                            expedition.put("Participants", participants);
                            database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                            break;
                        }
                    }
                }
            }
        });
    }

    public void removeParticipant(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");
                    for (Map<String, Object> participant : participants) {
                        if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            participant.put("Joined", false);
                            break;
                        }
                    }
                    expedition.put("Participants", participants);
                    database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("GVIDI", "Participant removed correctly");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("GVIDI", "Participant cannot be removed");
                                }
                            });
                }
            }
        });
    }

    public void checkIfParticipantIsJoined() {
        try {
            mutex.acquire(1);

            // By default, the participant must join an expedition
            MainActivity.b_canJoinExpedition = true;
            CollectionReference collectionReference = database.collection(DATABASE_NAME);
            collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        // Check if the current user is already enrolled in an expedition
                        for (DocumentSnapshot document : documents) {
                            Map<String, Object> expedition = document.getData();
                            if ((boolean) expedition.get("InProgress")) {
                                ArrayList<Map<String, Object>> participants =
                                        (ArrayList<Map<String, Object>>) expedition.get("Participants");
                                for (Map<String, Object> participant : participants) {
                                    if (participant.get("id").equals(FirebaseAuth.getInstance()
                                            .getCurrentUser().getEmail())) {
                                        if ((boolean) participant.get("Joined")) {
                                            MainActivity.expedition_name = (String) expedition.get("ExpName");
                                            MainActivity.b_canJoinExpedition = false;
                                            MainActivity.b_canLeaveExpedition = true;
                                            MainActivity.b_canCreateExpedition = false;
                                            MainActivity.b_canCancelExpedition = false;
                                        } else {
                                            MainActivity.b_canJoinExpedition = true;
                                            MainActivity.b_canLeaveExpedition = false;
                                            MainActivity.b_canCreateExpedition = false;
                                            MainActivity.b_canCancelExpedition = false;
                                        }
                                        return;
                                    }
                                }
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

    public void addAlert(final String expedition_name, final String latitude, final String longitude, final String alert_type) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();

                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    for (Map<String, Object> participant : participants) {
                        try {
                            if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                ArrayList<Map<String, Object>> alerts =
                                        (ArrayList<Map<String, Object>>) participant.get("Alerts");

                                Map<String, Object> new_alert = new HashMap<>();
                                new_alert.put("AlertType", alert_type);
                                new_alert.put("Latitude", participant.get("Lat"));
                                new_alert.put("Longitude", participant.get("Lon"));
                                new_alert.put("Timestamp", System.currentTimeMillis() / 1000);

                                alerts.add(new_alert);
                                participant.put("Alerts", alerts);
                                expedition.put("Participants", participants);
                                database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("GVIDI", "Alert added correctly");
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("GVIDI", "Alert cannot be added");
                                            }
                                        });
                                break;
                            }
                        } catch (Exception ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    public void fillParticipantAlerts(final String expedition_name, final String participant_email, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();

                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    ListView listView = activity.findViewById(R.id.list_view_alerts);
                    ArrayList<AlertListViewItem> items = new ArrayList<>();
                    AlertListAdapter adapter = new AlertListAdapter(activity, items);
                    listView.setAdapter(adapter);

                    for (Map<String, Object> participant : participants) {
                        if (participant.get("id").equals(participant_email)) {
                            // Get the alerts of the participant
                            ArrayList<Map<String, Object>> alerts =
                                    (ArrayList<Map<String, Object>>) participant.get("Alerts");
                            ImageView imageView = new ImageView(activity);
                            for (Map<String, Object> alert : alerts) {
                                // SOS ALERT
                                if (alert.get("AlertType").equals("SOS")) {
                                    imageView.setImageResource(R.drawable.sos_32);
                                } else if (alert.get("AlertType").equals("STOP")) {
                                    imageView.setImageResource(R.drawable.stop_32);
                                }

                                try {
                                    String coordinates_field = String.format(Locale.UK, "Lat: %.3f, Lon: %.3f",
                                            Double.parseDouble(alert.get("Latitude").toString()),
                                            Double.parseDouble(alert.get("Longitude").toString()));
                                    String date = ExpeditionManagement.getDateCurrentTimeZone((long) alert.get("Timestamp"));
                                    items.add(new AlertListViewItem(date, coordinates_field, imageView));
                                } catch (Exception ex) {
                                    Log.e("GVIDI", ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void fillParticipantDetails(final String expedition_name, final String participant_email, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();

                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    TextView tv_participant_name = activity.findViewById(R.id.lbl_participant_name_value);
                    TextView tv_participant_email = activity.findViewById(R.id.lbl_participant_email_value);
                    TextView tv_participant_coord = activity.findViewById(R.id.lbl_participant_coord_value);
                    TextView tv_participant_battery = activity.findViewById(R.id.lbl_participant_battery_value);
                    TextView tv_participant_distance = activity.findViewById(R.id.lbl_distance_value);
                    TextView tv_participant_avg_pace = activity.findViewById(R.id.lbl_avg_pace_value);
                    TextView tv_particpant_current_pace = activity.findViewById(R.id.lbl_current_pace_value);

                    for (Map<String, Object> participant : participants) {
                        if (participant.get("id").equals(participant_email)) {
                            // Get all the participant information and put this information in
                            // the corresponding textFields
                            tv_participant_name.setText(participant.get("id").toString().split("@")[0]);
                            tv_participant_email.setText(participant.get("id").toString());
                            tv_participant_coord.setText(String.format(Locale.UK, "%s: %s, %s: %s",
                                    activity.getResources().getString(R.string.str_lat),
                                    participant.get("Lat").toString(),
                                    activity.getResources().getString(R.string.str_lon),
                                    participant.get("Lon").toString()));

                            // Depending on the battery level, change the text color
                            if ((long) participant.get("BatteryLevel") >= 50) {
                                tv_participant_battery.setTextColor(activity.getResources().getColor(R.color.colorLogoLight));
                            } else if ((long) participant.get("BatteryLevel") > 20) {
                                tv_participant_battery.setTextColor(activity.getResources().getColor(R.color.orangeColor));
                            } else
                                tv_participant_battery.setTextColor(activity.getResources().getColor(R.color.redColor));

                            // Establish the battery level and the traveled distance
                            tv_participant_battery.setText(String.format(Locale.UK, "%s %s",
                                    participant.get("BatteryLevel").toString(),
                                    activity.getResources().getString(R.string.percent)));
                            String units_string;
                            double total_distance = (double) participant.get("TotalDistance");
                            if (total_distance < 1) {
                                total_distance *= 1000;
                                units_string = activity.getResources().getString(R.string.str_meters);
                            } else {
                                units_string = activity.getResources().getString(R.string.str_km);
                            }
                            tv_participant_distance.setText(String.format(Locale.UK, "%.2f %s", total_distance, units_string));

                            // Calculate and show the average pace
                            total_distance = (double) participant.get("TotalDistance");
                            long current_timestamp = System.currentTimeMillis() / 1000;
                            long delta_time = current_timestamp - (long) participant.get("InitTime");
                            // Get time in minutes
                            delta_time /= 60;
                            double pace = delta_time / total_distance;
                            tv_participant_avg_pace.setText(String.format(Locale.UK, "%.2f %s", pace,
                                    activity.getResources().getString(R.string.str_pace)));

                            // Calculate and show the current pace
                            ArrayList<Map<String, Object>> previousLocations =
                                    (ArrayList<Map<String, Object>>) participant.get("PreviousLocations");
                            double prev_lat = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                    get(previousLocations.size() - 1).get("Lat").toString())));
                            double prev_lon = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                    get(previousLocations.size() - 1).get("Lon").toString())));
                            long prev_timestamp = (long) previousLocations.get(previousLocations
                                    .size() - 1).get("Timestamp");

                            // Get the current location
                            double current_lat = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lat").toString())));
                            double current_lon = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lon").toString())));
                            current_timestamp = System.currentTimeMillis() / 1000;

                            // Get the time elapsed between two location updates (in minutes)
                            delta_time = (current_timestamp - prev_timestamp) / 60;
                            // Get the distance (in kms) between two points
                            double distance = getDistanceBetweenTwoPoints(prev_lat, prev_lon, current_lat, current_lon);

                            pace = delta_time / distance;
                            if (pace > 100 || pace == 0) {
                                tv_particpant_current_pace.setText(String.format(Locale.UK, "%s",
                                        activity.getResources().getString(R.string.str_stopped)));
                            } else {
                                tv_particpant_current_pace.setText(String.format(Locale.UK, "%.2f %s", pace,
                                        activity.getResources().getString(R.string.str_pace)));
                            }
                        }
                    }
                }
            }
        });
    }

    public void fillMainGUI(final String expedition_name, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");

                    TextView label_total_distance = activity.findViewById(R.id.label_total_distance_value);

                    try {
                        if (guide.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            double distance = (double) guide.get("TotalDistance");
                            String unit;
                            if (distance < 1) {
                                distance *= 1000;
                                unit = activity.getResources().getString(R.string.str_meters);
                            } else {
                                unit = activity.getResources().getString(R.string.str_km);
                            }
                            label_total_distance.setText(String.format(Locale.UK, "%.2f %s", distance, unit));
                        } else {
                            ArrayList<Map<String, Object>> participants =
                                    (ArrayList<Map<String, Object>>) expedition.get("Participants");
                            for (Map<String, Object> participant : participants) {
                                if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                    double distance = (double) participant.get("TotalDistance");
                                    String unit;
                                    if (distance < 1) {
                                        distance *= 1000;
                                        unit = activity.getResources().getString(R.string.str_meters);
                                    } else {
                                        unit = activity.getResources().getString(R.string.str_km);
                                    }
                                    label_total_distance.setText(String.format(Locale.UK, "%.2f %s", distance, unit));
                                    break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("GVIDI", ex.getMessage());
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
                        ArrayList<Map<String, Object>> participants =
                                (ArrayList<Map<String, Object>>) expedition.get("Participants");
                        for (Map<String, Object> participant : participants) {
                            try {
                                if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                    participant.put("BatteryLevel", battery_level);
                                    break;
                                }
                            } catch (NullPointerException ex) {
                                Log.e("GVIDI", ex.getMessage());
                            }
                        }
                        expedition.put("Participants", participants);
                        database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                    }
                }
            });
        }
    }

    public void calculateInstantaneousPace(final String expedition_name, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");
                    for (Map<String, Object> participant : participants) {
                        try {
                            if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                ArrayList<Map<String, Object>> previousLocations =
                                        (ArrayList<Map<String, Object>>) participant.get("PreviousLocations");

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
                                    double current_lat = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lat").toString())));
                                    double current_lon = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lon").toString())));
                                    long current_timestamp = System.currentTimeMillis() / 1000;

                                    // Get the time elapsed between two location updates (in minutes)
                                    double delta_time = (current_timestamp - prev_timestamp) / 60.0;
                                    // Get the distance (in kms) between two points
                                    double distance = getDistanceBetweenTwoPoints(prev_lat, prev_lon, current_lat, current_lon);

                                    double pace = delta_time / distance;
                                    TextView lbl_pace = activity.findViewById(R.id.label_pace_value);

                                    if (pace > 100 || pace == 0) {
                                        lbl_pace.setText(String.format(Locale.UK, "%s",
                                                activity.getResources().getString(R.string.str_stopped)));
                                    } else {
                                        lbl_pace.setText(String.format(Locale.UK, "%.2f %s", pace,
                                                activity.getResources().getString(R.string.str_pace)));
                                    }
                                } catch (Exception ex) {
                                    Log.e("GVIDI", ex.getMessage());
                                }
                                break;
                            }
                        } catch (NullPointerException ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    public void calculateAveragePace(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");
                    for (Map<String, Object> participant : participants) {
                        try {
                            if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                double total_distance = (double) participant.get("TotalDistance");
                                long current_time = System.currentTimeMillis() / 1000;
                                long delta_time = current_time - (long) participant.get("InitTime");
                                // Get time in minutes
                                delta_time /= 60;
                                double pace = delta_time / total_distance;

                                break;
                            }
                        } catch (NullPointerException ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    public void updatePosition(final String expedition_name, final String latitude, final String longitude) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");
                    for (Map<String, Object> participant : participants) {
                        try {
                            if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                // Update the previousLocations array, pushing the coordinates that are
                                // going to be updated
                                ArrayList<Map<String, Object>> previousLocations =
                                        (ArrayList<Map<String, Object>>) participant.get("PreviousLocations");
                                Map<String, Object> prev_loc = new HashMap<>();
                                prev_loc.put("Lat", participant.get("Lat"));
                                prev_loc.put("Lon", participant.get("Lon"));
                                prev_loc.put("Timestamp", System.currentTimeMillis() / 1000);
                                previousLocations.add(prev_loc);

                                // Update the current coordinates by the new ones
                                participant.put("PreviousLocations", previousLocations);
                                participant.put("Lat", latitude);
                                participant.put("Lon", longitude);

                                // Check if there is necessary to add a distance alert. If it is
                                // necessary, add it
                                Map<String, Object> guide = (Map<String, Object>)expedition.get("Guide");
                                double guide_lat = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lat").toString())));
                                double guide_lon = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lon").toString())));
                                double current_lat = Math.toRadians(Math.abs(Double.parseDouble(latitude)));
                                double current_lon = Math.toRadians(Math.abs(Double.parseDouble(longitude)));

                                double distance = getDistanceBetweenTwoPoints(guide_lat, guide_lon, current_lat, current_lon);
                                // If the distance between the participant and the guide is more than 0.5kms add a distance alert
                                // so the guide should go slower
                                if (distance > 0.5) {
                                    addAlert(expedition_name, latitude, longitude, "DISTANCE");
                                }
                                // If the distance between the participant and the guide is greater than 1km, add a lost alert
                                // The participant is pretty far away, so the guide should stop the expedition 
                                else if (distance > 1) {
                                    addAlert(expedition_name, latitude, longitude, "LOST");
                                }

                                expedition.put("Participants", participants);
                                database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                                break;
                            }
                        } catch (NullPointerException ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }
                    }
                    updateParticipantTotalDistance(expedition_name);
                }
            }
        });
    }

    public void updateParticipantTotalDistance(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");
                    for (Map<String, Object> participant : participants) {
                        try {
                            if (participant.get("id").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                ArrayList<Map<String, Object>> previousLocations =
                                        (ArrayList<Map<String, Object>>) participant.get("PreviousLocations");

                                // Get the coordinates of the last element in the previousLocations array
                                if (previousLocations.size() > 0) {
                                    try {
                                        double prev_lat = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                                get(previousLocations.size() - 1).get("Lat").toString())));
                                        double prev_lon = Math.toRadians(Math.abs(Double.parseDouble(previousLocations.
                                                get(previousLocations.size() - 1).get("Lon").toString())));

                                        // Get the current location
                                        double current_lat = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lat").toString())));
                                        double current_lon = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lon").toString())));

                                        double distance = getDistanceBetweenTwoPoints(prev_lat, prev_lon, current_lat, current_lon);
                                        // Update the total distance of the participant
                                        participant.put("TotalDistance", distance +
                                                Double.parseDouble(participant.get("TotalDistance").toString()));
                                        expedition.put("Participants", participants);
                                        database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                                        break;
                                    } catch (Exception ex) {
                                        Log.e("GVIDI", ex.getMessage());
                                    }
                                }
                            }
                        } catch (NullPointerException ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }
                    }
                }
            }
        });
    }
}
