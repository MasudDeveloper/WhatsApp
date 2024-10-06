package com.mrdeveloper.whatsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    BottomNavigationView bottomNavigationView;

    FirebaseAuth myAuth;
    FirebaseUser currentUser;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        replaceFragment(new ChatFragment());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.chat) {
                    replaceFragment(new ChatFragment());
                    return true;
                } else if (item.getItemId() == R.id.find_friends) {
                    replaceFragment(new FindFriendsFragment());
                    return true;
                } else if (item.getItemId() == R.id.friendRequest) {
                    replaceFragment(new FriendRequestFragment());
                    return true;
                } else if (item.getItemId() == R.id.group) {
                    replaceFragment(new GroupFragment());
                    return true;
                } else {
                    replaceFragment(new ContractsFragment());
                    return true;
                }
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.camera) {

                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},100);
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(cameraIntent);
                    }

                } else if (item.getItemId() == R.id.search) {

                }else if (item.getItemId() == R.id.createGroup) {

                } else if (item.getItemId() == R.id.find_friends) {

                    startActivity(new Intent(MainActivity.this,FindFriendsActivity.class));

                } else if (item.getItemId() == R.id.settings) {

                    startActivity(new Intent(MainActivity.this,Settings.class));

                } else {
                    myAuth.signOut();
                    startActivity(new Intent(MainActivity.this,Login.class));
                }

                return true;
            }
        });






    } // ========================= On create End ===========================

    public void replaceFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this,Login.class));
            finish();
        } else {

            String currentUID = myAuth.getCurrentUser().getUid();

            dbRef.child("Users").child(currentUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("name").exists()){

                    } else {
                        startActivity(new Intent(MainActivity.this,UpdateProfile.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

    } // =======================================================

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {



            }
        }
    });


}