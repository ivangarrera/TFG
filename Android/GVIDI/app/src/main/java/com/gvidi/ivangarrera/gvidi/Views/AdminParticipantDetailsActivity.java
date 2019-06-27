package com.example.ivangarrera.example.Views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.ivangarrera.example.Controller.AlertListViewItem;
import com.example.ivangarrera.example.Controller.ParticipantManagementController;
import com.example.ivangarrera.example.R;

public class AdminParticipantDetailsActivity extends AppCompatActivity {
    private static final String TAG = "GVIDI";
    private ImageView img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_participant_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        img_back = findViewById(R.id.img_back_participant_details);

        final ListView listView = findViewById(R.id.list_view_alerts);

        ParticipantManagementController participantManagement = new ParticipantManagementController(null);

        if (!MainActivity.expedition_name.isEmpty() &&
                !MainActivity.participant_selected_to_see_details.isEmpty()) {
            participantManagement.fillParticipantDetails(MainActivity.expedition_name,
                    MainActivity.participant_selected_to_see_details, this);
            participantManagement.fillParticipantAlerts(MainActivity.expedition_name,
                    MainActivity.participant_selected_to_see_details, this);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertListViewItem item = (AlertListViewItem) listView.getItemAtPosition(position);
                Log.d(TAG, item.getCoordinates());
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
