package com.example.ivangarrera.example.Views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ivangarrera.example.Controller.ExpeditionManagementController;
import com.example.ivangarrera.example.Controller.GuideManagementController;
import com.example.ivangarrera.example.Model.ExpeditionManagement;
import com.example.ivangarrera.example.Model.GuideManagement;
import com.example.ivangarrera.example.R;

public class CreateExpeditionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expedition);
        Toolbar toolbar = findViewById(R.id.toolbar_create_exp);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e("GVIDI", ex.getMessage());
        }

        final EditText et_createExp = findViewById(R.id.et_expName);
        Button btn_createExp = findViewById(R.id.btn_createExpedition);

        btn_createExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_createExp.getText().toString().length() >= 5) {
                    GuideManagementController guideManagement = new GuideManagementController(null);
                    ExpeditionManagementController expeditionManagement = new ExpeditionManagementController();

                    expeditionManagement.addExpedition(et_createExp.getText().toString());
                    guideManagement.addGuide(et_createExp.getText().toString());
                    MainActivity.b_canCreateExpedition = false;
                    MainActivity.b_canCancelExpedition = true;
                    MainActivity.expedition_name = et_createExp.getText().toString();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "The name of the expedition must" +
                            " be at least 5 characters long", Toast.LENGTH_SHORT);
                }
            }
        });
    }
}
