package com.example.ivangarrera.example.Views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ivangarrera.example.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Switch btnSignIn, btnSignUp;
    private Button btnNewPassword, btnGo;
    private EditText etName, etEmail, etPassword;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLogin = false;

        // Get Firebase authentication instance
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivityForResult(new Intent(SignupActivity.this, MainActivity.class), 1);
        }

        setResult(0, null);

        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.toolbar_signup);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e("GVIDI", ex.getMessage());
        }

        btnSignIn = findViewById(R.id.btn_signin);
        btnSignUp = findViewById(R.id.btn_signup);
        btnGo = findViewById(R.id.btn_go);
        btnNewPassword = findViewById(R.id.btn_new_password);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Email and password fields must be filled", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isLogin) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                            SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.getException()!=null) {
                                        Log.e("GVIDI", "onComplete: Failed=" + task.getException().getMessage());
                                    }
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Authentication has failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );
                } else {
                    if (!etName.getText().toString().trim().isEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                                SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // Check if the authentication has been successful
                                        if (task.getException() != null) {
                                            Log.e("GVIDI", "onComplete: Failed=" + task.getException().getMessage());
                                        }
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this, "The authentication has failed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.
                                                    Builder().setDisplayName(etName.getText().toString()).build());
                                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    }
                                }
                        );
                    }
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin) {
                    isLogin = false;
                    btnSignIn.setChecked(false);
                    btnSignUp.setChecked(true);
                    etName.setVisibility(View.VISIBLE);
                } else {
                    isLogin = true;
                    btnSignIn.setChecked(true);
                    btnSignUp.setChecked(false);
                    etName.setVisibility(View.GONE);
                }
            }
        });


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLogin) {
                    isLogin = true;
                    etName.setVisibility(View.GONE);
                    btnSignIn.setChecked(true);
                    btnSignUp.setChecked(false);
                } else {
                    isLogin = false;
                    etName.setVisibility(View.VISIBLE);
                    btnSignIn.setChecked(false);
                    btnSignUp.setChecked(true);
                }
            }
        });

        btnNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == 0) {
            this.finish();
        }
    }
}
