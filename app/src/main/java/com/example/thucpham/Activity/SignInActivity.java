package com.example.thucpham.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thucpham.R;
import com.example.thucpham.constant.Profile;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SignInActivity"; // Tag ghi log.
    private LinearLayout layoutSignUp;
    private TextInputLayout formEmail, formPassword;
    private Button btnSignIn;
    private ProgressBar progressBar;
    private CheckBox mChkRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initUI();
        getDataFromSharedPreferences();
        getSupportActionBar().hide();
    }

    private void initUI() {
        layoutSignUp = findViewById(R.id.layout_SignInActivity_signIn);
        btnSignIn = findViewById(R.id.btn_SignInActivity_signIn);
        progressBar = findViewById(R.id.progressBar_SignInActivity_loadingLogin);
        formEmail = findViewById(R.id.form_SignInActivity_email);
        formPassword = findViewById(R.id.form_SignInActivity_password);
        mChkRemember = findViewById(R.id.chk_sign_in_activity_remember);
        progressBar.setVisibility(View.INVISIBLE);
        layoutSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_SignInActivity_signIn:
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                break;
            case R.id.btn_SignInActivity_signIn:
                if (login()) return;
                break;
        }
    }

    private boolean login() {
        String email = formEmail.getEditText().getText().toString().trim();
        String password = formPassword.getEditText().getText().toString().trim();

        if (email.equals("admin") && password.equals("admin")) {
            rememberUser("admin", "admin", email, password);
            startMainActivity();
            return true;
        } else {
            userLogin(email, password);
            return false;
        }
    }

    private void userLogin(String phoneNumber, String passwordUser) {
        formEmail.setError(null);
        formPassword.setError(null);

        if (!validate(phoneNumber, passwordUser)) return;

        progressBar.setVisibility(View.VISIBLE);
        final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("User").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.INVISIBLE);
                if (snapshot.exists()) {
                    String password = snapshot.child("password").getValue(String.class);
                    if (password.equals(passwordUser)) {
                        rememberUser("user", phoneNumber, phoneNumber, passwordUser);
                        startMainActivity();
                    } else {
                        formPassword.setError("Mật khẩu không đúng");
                    }
                } else {
                    formEmail.setError("Tài khoản không tồn tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void rememberUser(String role, String id, String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("My_User", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", email);
        editor.putString("password", password);
        editor.putString("role", role);
        editor.putString("id", id);
        editor.putBoolean("remember", mChkRemember.isChecked());
        editor.apply();
    }

    private void getDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("User_file", MODE_PRIVATE);
        boolean isRemember = sharedPreferences.getBoolean("remember", false);
        if (isRemember) {
            formEmail.getEditText().setText(sharedPreferences.getString("username", ""));
            formPassword.getEditText().setText(sharedPreferences.getString("password", ""));
        }
    }

    private boolean validate(String email, String password) {
        if (email.isEmpty() && password.isEmpty()) {
            formEmail.setError("Không được bỏ trống số điện thoại");
            formPassword.setError("Không được bỏ trống mật khẩu");
            return false;
        } else if (email.isEmpty()) {
            formEmail.setError("Không được bỏ trống số điện thoại");
            return false;
        } else if (password.isEmpty()) {
            formPassword.setError("Không được bỏ trống mật khẩu");
            return false;
        }
        return true;
    }

    private void startMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}
