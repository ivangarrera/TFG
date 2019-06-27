package com.example.ivangarrera.example.Model;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivangarrera.example.Controller.AlertListAdapter;
import com.example.ivangarrera.example.Controller.AlertListViewItem;
import com.example.ivangarrera.example.Controller.MyItemAdapter;
import com.example.ivangarrera.example.Controller.MyListViewItem;
import com.example.ivangarrera.example.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ExpeditionManagement {
    private final String DATABASE_NAME = "Expeditions";
    private FirebaseFirestore database;

    public ExpeditionManagement() {
        database = FirebaseFirestore.getInstance();
    }

    public void addExpedition(String expedition_name) {
        Map<String, Object> expedition = new HashMap<>();
        ArrayList<Map<String, Object>> participants = new ArrayList<>();
        ArrayList<Map<String, Object>> general_alerts = new ArrayList<>();

        expedition.put("ExpName", expedition_name);
        expedition.put("Guide", "");
        expedition.put("InProgress", true);
        expedition.put("Participants", participants);
        expedition.put("GeneralAlerts", general_alerts);
        expedition.put("InitTime", System.currentTimeMillis() / 1000);
        expedition.put("LastStop", System.currentTimeMillis() / 1000);

        database.collection(DATABASE_NAME).document(expedition_name).set(expedition)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d("GVIDI", "Expedition added correctly");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("GVIDI", "Expedition cannot be added");
                    }
                });
    }

    public void closeExpedition(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    expedition.put("InProgress", false);
                    expedition.put("EndTime", System.currentTimeMillis() / 1000);
                    long init_time = (long) expedition.get("InitTime");
                    long end_time = (long) expedition.get("EndTime");
                    expedition.put("Duration", end_time - init_time);

                    database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("GVIDI", "Expedition closed correctly");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("GVIDI", "Expedition cannot be closed");
                                }
                            });
                }
            }
        });
    }

    public void fillExpeditionsToJoin(final Activity activity) {
        CollectionReference collectionReference = database.collection(DATABASE_NAME);
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    ListView listView = activity.findViewById(R.id.list_view_join);
                    ArrayList<MyListViewItem> items = new ArrayList<>();
                    MyItemAdapter adapter = new MyItemAdapter(activity, items);
                    listView.setAdapter(adapter);

                    for (DocumentSnapshot document : documents) {
                        Map<String, Object> expedition = document.getData();
                        if ((boolean) expedition.get("InProgress")) {
                            String expedition_name = expedition.get("ExpName").toString();
                            Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");
                            items.add(new MyListViewItem(expedition_name, guide.get("id").toString()));
                        }
                    }
                    // Show changes
                    activity.findViewById(R.id.pb_join_expedition).setVisibility(View.GONE);
                    activity.findViewById(R.id.tv_obtaining).setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void fillAllParticipantAlerts(final String expedition_name, final Activity activity,
                                         final int interval_in_seconds) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();

                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    ListView listView = activity.findViewById(R.id.lv_alerts_last_hours);
                    ArrayList<AlertListViewItem> items = new ArrayList<>();
                    AlertListAdapter adapter = new AlertListAdapter(activity, items);
                    listView.setAdapter(adapter);

                    for (Map<String, Object> participant : participants) {
                        // Get the alerts of the participant
                        ArrayList<Map<String, Object>> alerts =
                                (ArrayList<Map<String, Object>>) participant.get("Alerts");
                        ImageView imageView = new ImageView(activity);
                        for (Map<String, Object> alert : alerts) {
                            // Check if the alert has occurred inside the interval
                            if ((System.currentTimeMillis() / 1000) - (long) alert.get("Timestamp")
                                    <= interval_in_seconds) {

                                // Set the icon which identify the type of the alert
                                if (alert.get("AlertType").equals("SOS")) {
                                    imageView.setImageResource(R.drawable.sos_32);
                                } else if (alert.get("AlertType").equals("STOP")) {
                                    imageView.setImageResource(R.drawable.stop_32);
                                }

                                // Establish the info about coordinates and the date the alert occurred
                                try {
                                    String coordinates_field = String.format(Locale.UK, "%s. Lat: %.3f, Lon: %.3f",
                                            participant.get("id").toString().split("@")[0],
                                            Double.parseDouble(alert.get("Latitude").toString()),
                                            Double.parseDouble(alert.get("Longitude").toString()));
                                    String date = getDateCurrentTimeZone((long) alert.get("Timestamp"));

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

    public void fillParticipantsMoreDistance(final String expedition_name, final Activity activity,
                                             final int maximum_distance) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    ListView listView = activity.findViewById(R.id.lv_participants_meters);
                    ArrayList<MyListViewItem> items = new ArrayList<>();
                    MyItemAdapter adapter = new MyItemAdapter(activity, items);
                    listView.setAdapter(adapter);

                    double guide_latitude = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lat").toString())));
                    double guide_longitude = Math.toRadians(Math.abs(Double.parseDouble(guide.get("Lon").toString())));

                    for (Map<String, Object> participant : participants) {
                        double participant_latitude = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lat").toString())));
                        double participant_longitude = Math.toRadians(Math.abs(Double.parseDouble(participant.get("Lon").toString())));

                        double distance = ParticipantManagement.getDistanceBetweenTwoPoints(guide_latitude,
                                guide_longitude, participant_latitude, participant_longitude);

                        double max_distance = (double) maximum_distance / 1000;
                        if (distance >= max_distance) {
                            String string_to_show;
                            if (distance < 1) {
                                distance *= 1000;
                                string_to_show = activity.getResources().getString(R.string.str_meters);
                            } else {
                                string_to_show = activity.getResources().getString(R.string.str_km);
                            }

                            items.add(new MyListViewItem(participant.get("id").toString().split("@")[0],
                                    String.format(Locale.UK, "%.2f %s", distance, string_to_show)));
                        }
                    }
                }
            }
        });
    }

    public void fillExpeditionStops(final String expedition_name, final Activity activity) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    long last_stop = (long) expedition.get("LastStop");
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    int participant_counter = 0;
                    for (Map<String, Object> participant : participants) {
                        ArrayList<Map<String, Object>> alerts =
                                (ArrayList<Map<String, Object>>) participant.get("Alerts");

                        alerts_for:
                        for (Map<String, Object> alert : alerts) {
                            // If the alert has been produced after the last stop
                            if ((long) alert.get("Timestamp") > last_stop &&
                                    alert.get("AlertType").equals("STOP")) {
                                participant_counter++;
                                break alerts_for;
                            }
                        }
                    }

                    // Show changes in the view
                    TextView number_stops = activity.findViewById(R.id.number_stops);
                    TextView stops_text = activity.findViewById(R.id.stops_string);
                    number_stops.setText(String.valueOf(participant_counter));
                    long amount_minutes = ((System.currentTimeMillis() / 1000) - last_stop) / 60;
                    stops_text.setText(String.format(Locale.UK, "%s %d %s", activity.getResources().getString(R.string.want_stop),
                            amount_minutes, activity.getResources().getString(R.string.str_min)));
                }
            }
        });
    }

    public void fillExpeditionBattery(final String expedition_name, final Activity activity, final int battery_level) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    int participant_counter = 0;
                    for (Map<String, Object> participant : participants) {
                        if ((long) participant.get("BatteryLevel") < battery_level) {
                            participant_counter++;
                        }
                    }

                    // Show changes in the view
                    TextView number_battery = activity.findViewById(R.id.number_battery);
                    number_battery.setText(String.valueOf(participant_counter));
                }
            }
        });
    }

    public void addDistanceAlert(final String expedition_name, final String other_participant_id) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    ArrayList<Map<String, Object>> general_alerts =
                            (ArrayList<Map<String, Object>>) expedition.get("GeneralAlerts");
                    Map<String, Object> new_alert = new HashMap<>();
                    new_alert.put("AlertType", "DistanceAlert");
                    new_alert.put("Timestamp", System.currentTimeMillis() / 1000);
                    new_alert.put("ParticipantID", other_participant_id);

                    general_alerts.add(new_alert);
                    expedition.put("GeneralAlerts", general_alerts);
                    database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("GVIDI", "Distance alert added correctly");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("GVIDI", "Distance alert cannot be added");
                                }
                            });
                }
            }
        });
    }

    public void updateLastStop(final String expedition_name) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    expedition.put("LastStop", System.currentTimeMillis() / 1000);
                    database.collection(DATABASE_NAME).document(expedition_name).set(expedition, SetOptions.merge());
                }
            }
        });
    }

    public void updateMap(final String expedition_name, final GoogleMap mMap) {
        DocumentReference documentReference = database.collection(DATABASE_NAME).document(expedition_name);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> expedition = task.getResult().getData();
                    Map<String, Object> guide = (Map<String, Object>) expedition.get("Guide");
                    ArrayList<Map<String, Object>> participants =
                            (ArrayList<Map<String, Object>>) expedition.get("Participants");

                    double guide_lat = 0.f, guide_lon = 0.f;
                    mMap.clear();
                    try {
                        guide_lat = Double.parseDouble(guide.get("Lat").toString());
                        guide_lon = Double.parseDouble(guide.get("Lon").toString());
                        LatLng location = new LatLng(guide_lat, guide_lon);

                        Marker m = mMap.addMarker(new MarkerOptions().position(location)
                                .title(guide.get("id").toString().split("@")[0]));
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                        m.setTag(guide.get("id").toString());
                    } catch (Exception ex) {
                        Log.e("GVIDI", ex.getMessage());
                    }

                    for (Map<String, Object> participant : participants) {
                        try {
                            double part_lat = Double.parseDouble(participant.get("Lat").toString());
                            double part_lon = Double.parseDouble(participant.get("Lon").toString());
                            double distance = ParticipantManagement.getDistanceBetweenTwoPoints(
                                    Math.toRadians(Math.abs(guide_lat)), Math.toRadians(Math.abs(guide_lon)),
                                    Math.toRadians(Math.abs(part_lat)), Math.toRadians(Math.abs(part_lon)));

                            // TODO: Set separation threshold
                            LatLng location = new LatLng(part_lat, part_lon);
                            if (distance > 0.5) {
                                Marker m = mMap.addMarker(new MarkerOptions().position(location)
                                        .title(participant.get("id").toString().split("@")[0]));
                                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                m.setTag(participant.get("id").toString());
                            } else {
                                Marker m = mMap.addMarker(new MarkerOptions().position(location)
                                        .title(participant.get("id").toString().split("@")[0]));
                                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                m.setTag(participant.get("id").toString());
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                        } catch (Exception ex) {
                            Log.e("GVIDI", ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    // Function extracted from https://stackoverflow.com/questions/18717111/to-get-date-and-time-from-timestamp-android
    protected static String getDateCurrentTimeZone(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date currentTimeZone = calendar.getTime();
            return sdf.format(currentTimeZone);
        } catch (Exception e) {
        }
        return "";
    }
}
