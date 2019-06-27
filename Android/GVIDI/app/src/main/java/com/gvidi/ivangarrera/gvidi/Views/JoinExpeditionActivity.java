package com.example.ivangarrera.example.Views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ivangarrera.example.Controller.ExpeditionManagementController;
import com.example.ivangarrera.example.Controller.MyItemAdapter;
import com.example.ivangarrera.example.Controller.MyListViewItem;
import com.example.ivangarrera.example.Controller.ParticipantManagementController;
import com.example.ivangarrera.example.R;

import java.util.ArrayList;

public class JoinExpeditionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_expedition);
        Toolbar toolbar = findViewById(R.id.toolbar_join_exp);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e("GVIDI", ex.getMessage());
        }

        ArrayList<MyListViewItem> items = new ArrayList<>();

        // Set our custom adapter to the ListView
        final ListView listView = findViewById(R.id.list_view_join);
        MyItemAdapter adapter = new MyItemAdapter(this, items);
        listView.setAdapter(adapter);

        ExpeditionManagementController expeditionManagement = new ExpeditionManagementController();
        final ParticipantManagementController participantManagement = new ParticipantManagementController(null);

        expeditionManagement.fillExpeditionsToJoin(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.b_canJoinExpedition = false;
                MainActivity.b_canLeaveExpedition = true;
                MainActivity.b_changedOutside = true;
                MyListViewItem item = (MyListViewItem) listView.getItemAtPosition(position);
                MainActivity.expedition_name = item.getExpeditionName();

                participantManagement.addParticipant(item.getExpeditionName());
                finish();
            }
        });
    }
}
