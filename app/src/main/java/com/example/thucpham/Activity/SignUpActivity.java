package com.example.thucpham.Activity;


import static com.example.thucpham.constant.Profile.FIELDS_EMPTY;
import static com.example.thucpham.constant.Profile.NUMBER_PHONE_INVALID;
import static com.example.thucpham.constant.Profile.PASSWORD_INVALID;
import static com.example.thucpham.constant.Profile.PASSWORD_NOT_MATCH;
import static com.example.thucpham.constant.Profile.REGEX_PHONE_NUMBER;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thucpham.Model.User;
import com.example.thucpham.R;
import com.example.thucpham.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private TextInputLayout mFormPhoneNumber, mFormUserName, mFormPassword, mFormConfirmPassword, mFormAddress;
    private Button mBtnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
        getSupportActionBar().hide();
    }

    private void initUI() {
        mFormPhoneNumber = findViewById(R.id.form_SignUpActivity_phone_number);
        mFormUserName = findViewById(R.id.form_SignUpActivity_user_name);
        mFormPassword = findViewById(R.id.form_SignUpActivity_password);
        mFormConfirmPassword = findViewById(R.id.form_SignUpActivity_confirmPassword);
        mFormAddress = findViewById(R.id.form_SignUpActivity_address);
        mBtnSignUp = findViewById(R.id.btn_SignUpActivity_signUp);
        mBtnSignUp.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        clearErrors();
        setErrorEmpty();

        String strPhoneNumber = Objects.requireNonNull(mFormPhoneNumber.getEditText()).getText().toString().trim();
        String strUserName = mFormUserName.getEditText().getText().toString().trim();
        String strPassword = mFormPassword.getEditText().getText().toString().trim();
        String strConfirmPassword = mFormConfirmPassword.getEditText().getText().toString().trim();
        String strAddress = mFormAddress.getEditText().getText().toString().trim();

        if (strPhoneNumber.isEmpty()
                || strUserName.isEmpty()
                || strPassword.isEmpty()
                || strConfirmPassword.isEmpty()
                || strAddress.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            User user = createUser(strPhoneNumber, strUserName, strPassword, strAddress);
            ProgressDialog progressDialog = Utils.createProgressDiaglog(SignUpActivity.this);
            progressDialog.show();

            DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
            rootReference.child("User").child(strPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        rootReference.child("User").child(strPhoneNumber).setValue(user)
                                .addOnCompleteListener(task -> onSignUpSuccess(progressDialog, strPhoneNumber, strPassword))
                                .addOnFailureListener(e -> onSignUpFailure(progressDialog));
                    } else {
                        progressDialog.dismiss();
                        mFormPhoneNumber.setError("Số điện thoại đã tồn tại");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Có lỗi khi tạo tài khoản, vui lòng thử lại sau", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onCancelled: ", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "signUp: ", e);
            Toast.makeText(SignUpActivity.this, "Có lỗi xảy ra, vui lòng thử lại sau", Toast.LENGTH_LONG).show();
        }
    }

    private void clearErrors() {
        mFormPhoneNumber.setError(null);
        mFormUserName.setError(null);
        mFormPassword.setError(null);
        mFormConfirmPassword.setError(null);
        mFormAddress.setError(null);
    }

    private User createUser(String phone, String name, String password, String address) {
        User user = new User();
        user.setPhoneNumber(phone);
        user.setName(name);
        user.setPassword(password);
        user.setAddress(address);
        user.setStrUriAvatar("");
        user.setId(phone);
        return user;
    }

    private void onSignUpSuccess(ProgressDialog progressDialog, String phone, String password) {
        progressDialog.dismiss();
        Toast.makeText(SignUpActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
        remember(phone, password, "user", phone);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finishAffinity();
    }

    private void onSignUpFailure(ProgressDialog progressDialog) {
        progressDialog.dismiss();
        Toast.makeText(SignUpActivity.this, "Tạo tài khoản thất bại", Toast.LENGTH_LONG).show();
    }

    private void setErrorEmpty() {
        if (mFormPhoneNumber.getEditText().getText().toString().isEmpty())
            mFormPhoneNumber.setError("Số điện thoại không được để trống");
        if (mFormUserName.getEditText().getText().toString().isEmpty())
            mFormUserName.setError("Họ tên không được để trống");
        if (mFormPassword.getEditText().getText().toString().isEmpty())
            mFormPassword.setError("Mật khẩu không được để trống");
        if (mFormConfirmPassword.getEditText().getText().toString().isEmpty())
            mFormConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
        if (mFormAddress.getEditText().getText().toString().isEmpty())
            mFormAddress.setError("Địa chỉ không được để trống");
    }

    private void remember(String user, String password, String role, String id) {
        SharedPreferences sharedPreferences = getSharedPreferences("My_User", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", user);
        editor.putString("password", password);
        editor.putString("role", role);
        editor.putString("id", id);
        editor.putBoolean("logged", true);
        editor.putBoolean("remember", true);
        editor.apply();
    }
}
