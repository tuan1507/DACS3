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

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "SignUpActivity";
    private TextInputLayout mFormPhoneNumber,
            mFormUserName,
            mFormPassword,
            mFormConfirmPassword,
            mFormAddress;
    private Button mBtnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
        getSupportActionBar().hide(); // Ẩn actionbar
    }

    private void initUI() {
        mFormPhoneNumber = findViewById(R.id.form_SignUpActivity_phone_number);
        mFormUserName = findViewById(R.id.form_SignUpActivity_user_name);
        mFormPassword = findViewById(R.id.form_SignUpActivity_password);
        mFormConfirmPassword = findViewById(R.id.form_SignUpActivity_confirmPassword);
        mFormAddress = findViewById(R.id.form_SignUpActivity_address);
        mBtnSignUp = findViewById(R.id.btn_SignUpActivity_signUp);
        setOnclickListener();
    }

    private void setOnclickListener() {
        mBtnSignUp.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_SignUpActivity_signUp:
                signUp();
                break;
        }
    }

    private void signUp() {
        final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        mFormPhoneNumber.setError(null);
        mFormPassword.setError(null);
        mFormConfirmPassword.setError(null);
        mFormUserName.setError(null);
        mFormAddress.setError(null);
        String strPhoneNumber = Objects.requireNonNull(mFormPhoneNumber.getEditText()).getText().toString().trim();
        String strUserName = mFormUserName.getEditText().getText().toString().trim();
        String strPassword = mFormPassword.getEditText().getText().toString().trim();
        String strConfirmPassword = mFormConfirmPassword.getEditText().getText().toString().trim();
        String strAddress = mFormAddress.getEditText().getText().toString().trim();
        try {
            validate(strPhoneNumber,
                    strUserName,
                    strPassword,
                    strConfirmPassword,
                    strAddress);
            User user = new User();
            user.setPhoneNumber(strPhoneNumber);
            user.setName(strUserName);
            user.setPassword(strPassword);
            user.setAddress(strAddress);
            user.setStrUriAvatar("");
            user.setId(strPhoneNumber);
            ProgressDialog progressDialog = Utils.createProgressDiaglog(SignUpActivity.this);
            progressDialog.show();
            rootReference.child("User").child(strPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Map<String, Object> userDataMap = user.toMap();
                        rootReference.child("User")
                                .child(strPhoneNumber)
                                .setValue(user)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        Toast.makeText(SignUpActivity.this
                                                        , "Tạo tài khoản thành công"
                                                        , Toast.LENGTH_SHORT)
                                                .show();
                                        remember(strPhoneNumber,strPassword,"user",strPhoneNumber);
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                        finishAffinity();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this
                                                        , "Tạo tài khoản thất bại"
                                                        , Toast.LENGTH_LONG)
                                                .show();
                                        progressDialog.dismiss();
                                    }
                                });
                    } else {
                        //TODO thông báo số điện thoại đã tồn tại tới view
                        progressDialog.dismiss();
                        mFormPhoneNumber.setError("Số điện thoại đã tồn tại");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SignUpActivity.this
                                    , "Có lỗi khi tạo tài khoản, vui lòng thử lại sau"
                                    , Toast.LENGTH_LONG)
                            .show();
                    Log.e(TAG, "onCancelled: ",error.toException() );
                }
            });
        } catch (NullPointerException e) {
            if (e.getMessage().equals(FIELDS_EMPTY)) {
                setErrorEmpty();
                //TODO thông báo lỗi khi empty
            } else {
                Log.e(TAG, "signUp: ", e);
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(NUMBER_PHONE_INVALID)) {
                mFormPhoneNumber.setError("Số điện thoại không hợp lệ");
            } else if (e.getMessage().equals(PASSWORD_INVALID)) {
                mFormPassword.setError("Mật khẩu không hợp lệ");
            } else if (e.getMessage().equals(PASSWORD_NOT_MATCH)) {
                mFormConfirmPassword.setError("Mật khẩu không trùng nhau");
            } else {
                Log.e(TAG, "signUp: ", e);
            }
        } catch (Exception e) {
            //TODO ngoại lệ gì đó chưa bắt được
            Log.e(TAG, "signUp: ", e);
        }
    }

    private void setErrorEmpty() {
        if (mFormPhoneNumber.getEditText().getText().toString().isEmpty()) {
            mFormPhoneNumber.setError("Số điện thoại không được để trống");
        }
        if (mFormUserName.getEditText().getText().toString().isEmpty()) {
            mFormUserName.setError("Họ tên không được để trống");
        }
        if (mFormPassword.getEditText().getText().toString().isEmpty()) {
            mFormPassword.setError("Mật khẩu không được để trống");
        }
        if (mFormConfirmPassword.getEditText().getText().toString().isEmpty()) {
            mFormConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
        }
        if (mFormAddress.getEditText().getText().toString().isEmpty()) {
            mFormAddress.setError("Địa chỉ không được để trống");
        }

    }

    private void validate(String strPhoneNumber,
                          String strUserName,
                          String strPassword,
                          String strConfirmPassword,
                          String strAddress) {
        if (strPhoneNumber.isEmpty()
                || strPassword.isEmpty()
                || strConfirmPassword.isEmpty()
                || strUserName.isEmpty()
                || strAddress.isEmpty())
            throw new NullPointerException(FIELDS_EMPTY);
        if (!strPhoneNumber.matches(REGEX_PHONE_NUMBER))
            throw  new IllegalArgumentException(NUMBER_PHONE_INVALID);
        if (strPassword.length() < 6)
            throw  new IllegalArgumentException(PASSWORD_INVALID);
        if (!strConfirmPassword.equals(strPassword))
            throw new IllegalArgumentException(PASSWORD_NOT_MATCH);
    }
    public void remember(String user,String password,String role, String id){
        SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", user);
        editor.putString("password", password);
        editor.putString("role", role);
        editor.putString("id", id);
        editor.putBoolean("logged",true);
        editor.putBoolean("remember", true);
        editor.apply();
    }
}