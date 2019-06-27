package com.example.ivangarrera.example.Views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ivangarrera.example.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText etEmail;
    private Button  btnNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);

        // Get firebase authentication instance
        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        btnNewPassword = findViewById(R.id.btn_go);

        btnNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.reset_email_failure),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(email).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this,
                                            getResources().getString(R.string.reset_instructions),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this,
                                            getResources().getString(R.string.reset_failure),
                                            Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            }
                        }
                );
            }
        });

    }

}
