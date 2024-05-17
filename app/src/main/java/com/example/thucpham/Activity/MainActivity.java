package com.example.thucpham.Activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.thucpham.Fragment.Profile.ProfileViewModel;
import com.example.thucpham.Model.Bill;
import com.example.thucpham.Model.Cart;
import com.example.thucpham.Model.User;
import com.example.thucpham.R;
import com.example.thucpham.Service.ConnectionReceiver;
import com.example.thucpham.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{
    public static final String TAG = "MainActivity";
    public static final int MY_REQUEST_CODE = 10;

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private AppBarConfiguration mAppBarConfiguration;

    private ActivityMainBinding binding;

    private ImageView ivAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;

    private User user;

    private ProfileViewModel profileViewModel;
    private List<Bill> listBill = new ArrayList<>();

    ConnectionReceiver connectionReceiver = new ConnectionReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);
        initUI();
        initViewModel();
        checkUser();
        SharedPreferences preferences1 = getSharedPreferences("Number",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences1.edit();
        String number = "0";
        editor.putString("number",""+number);
        editor.apply();
        getBill();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_Product,
                R.id.nav_Bill,
                R.id.nav_Food)
                .setOpenableLayout(mDrawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);


    }
    public void loadUserInfoById(String phoneNumber){
        Log.d(TAG, "loadUserInfoById: ");
        Log.d(TAG, "loadUserInfoById: " + phoneNumber);
        final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("User").child(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: ");
                        if (snapshot.exists()) {
                            MainActivity.this.user = snapshot.getValue(User.class);
                            Log.d(TAG, "onDataChange: " + user);
                            profileViewModel.setUser(user);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: ", error.toException());
                    }
                });
    }
    //TODO: thử chuyển method sang ProfileFragment
    private final ActivityResultLauncher<Intent> mActivityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult()
                    , new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent intent = result.getData();
                                if (intent != null && intent.getData() != null) {
                                    Uri uriImage = intent.getData();
                                    Bitmap selectedImageBitmap = null;
                                    try {
                                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImage);
                                        Log.d(TAG, "onActivityResult: " + selectedImageBitmap.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "onActivityResult: ");
                                    profileViewModel.setBitmapImageAvatar(selectedImageBitmap);
                                }
                            }
                        }
                    });
    public void checkUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");
        String userRule = sharedPreferences.getString("role","");
        String userId = sharedPreferences.getString("id", "");

        if (userRule.equals("admin")){
            Log.d(TAG, "checkUser: admin");
            mNavigationView.setVisibility(View.VISIBLE);
            mNavigationView.getMenu().findItem(R.id.nav_Food).setVisible(false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else if (userRule.equals("user")) {
            loadUserInfoById(username);
            setUserViewModelObserver();
            mNavigationView.setVisibility(View.GONE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            Log.d(TAG, "checkUser: user");
        } else {
            mNavigationView.setVisibility(View.GONE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

    }


    @Deprecated
    private void setUserViewModelObserver() {
        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user1) {
                Log.d(TAG, "onChanged: change user information");
                tvUserEmail.setText(user1.getPhoneNumber());
                tvUserName.setText(user1.getName());
                SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
                String password = sharedPreferences.getString("password","");
                if (!user1.getPassword().equals(password)) {
                    sharedPreferences.edit().putString("password", user1.getPassword()).commit();
                }
                Glide.with(MainActivity.this)
                        .load(user1.getStrUriAvatar())
                        .error(R.drawable.ic_avatar_default)
                        .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                        .into(ivAvatar);
                Log.d(TAG, "onChanged: " + user1.toString());
            }

        };
        profileViewModel.getUser().observe(this, userObserver);
    }

    private void initViewModel() {
        this.profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

    }

    private void initUI() {
        mDrawerLayout = binding.drawerLayout;
        mNavigationView = binding.navView;
        ivAvatar = mNavigationView.getHeaderView(0).findViewById(R.id.iv_MainActivity_avatar);
        tvUserName = mNavigationView.getHeaderView(0).findViewById(R.id.tv_MainActivity_username);
        tvUserEmail = mNavigationView.getHeaderView(0).findViewById(R.id.tv_MainActivity_userEmail);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem menuItem = menu.findItem(R.id.btn_Actionbar_cart);
        View actionView = menuItem.getActionView();
        MenuItem menuItem1 = menu.findItem(R.id.searchProduct);
        menuItem1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
                return true;
            }
        });

        TextView cartBadgeTextView = actionView.findViewById(R.id.tv_CartActionItem_cart_badge);
        cartBadgeTextView.setVisibility(View.GONE);
        SharedPreferences preferences = getSharedPreferences("My_User",MODE_PRIVATE);
        String user = preferences.getString("username","");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Cart");
        List<Cart> list1 = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list1.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Cart cart = snap.getValue(Cart.class);
                    if (cart.getUserClient().equals(user)) {
                        list1.add(cart);

                    }
                }
                cartBadgeTextView.setText(String.valueOf(list1.size()));
                cartBadgeTextView.setVisibility(list1.size() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            }
        });
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("My_User",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged", false);
        editor.commit();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Please enable read external permission !");
                Toast.makeText(this, "Please enable read external permission !", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select picture"));
        Log.d(TAG, "openGallery: openGallery method");
    }
    public void getBill(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Bill");
        SharedPreferences preferences = getSharedPreferences("My_User", Context.MODE_PRIVATE);
        String user = preferences.getString("username","");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listBill.clear();
                for (DataSnapshot snap : snapshot.getChildren()){
                    Bill bill = snap.getValue(Bill.class);
                    if (user.equals(bill.getIdPartner()) && bill.getStatus().equals("No")) {
                        listBill.add(bill);
                    }else if (user.equals(bill.getIdClient()) && bill.getStatus().equals("No")){
                        listBill.add(bill);
                    }
                }
                SharedPreferences preferences1 = getSharedPreferences("Number",MODE_PRIVATE);
                int number = Integer.parseInt(preferences1.getString("number",""));
                if (listBill.size()>number ){
                    notification();
                    SharedPreferences.Editor editor = preferences1.edit();
                    editor.putString("number", String.valueOf(listBill.size()));
                    editor.apply();
                }else {
                    SharedPreferences.Editor editor = preferences1.edit();
                    editor.putString("number", String.valueOf(listBill.size()));
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public  void notification(){
        String CHANNEL_ID="1234";

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getPackageName() + "/" + R.raw.sound);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, "Thông báo", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription("Chuông thông báo");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            mChannel.setSound(soundUri, audioAttributes);
            mChannel.setVibrationPattern( new long []{ 100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 }) ;

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel( mChannel );
            }
        }

        NotificationCompat.Builder status = new NotificationCompat.Builder(this,CHANNEL_ID);
        status.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Bạn có đơn hàng mới")
                .setDefaults(Notification.DEFAULT_LIGHTS )
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +getPackageName()+"/"+R.raw.sound))
                .build();


        mNotificationManager.notify((int)System.currentTimeMillis(), status.build());

    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(connectionReceiver);
        super.onStop();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}