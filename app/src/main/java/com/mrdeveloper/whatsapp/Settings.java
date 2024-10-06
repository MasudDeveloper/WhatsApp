package com.mrdeveloper.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Settings extends AppCompatActivity {

    LinearLayout profileLayout;
    TextView tvName, tvBio;
    ImageView profileImage;

    FirebaseAuth myAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileLayout = findViewById(R.id.profileLayout);
        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBio);
        profileImage = findViewById(R.id.profileImage);

        myAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();


        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this, UpdateProfile.class));
            }
        });

        getAllDate();


    } // =====================================================

    private void getAllDate() {

        String currentUID = myAuth.getCurrentUser().getUid();

        dbRef.child("Users").child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChild("image")) {
                    String getName = snapshot.child("name").getValue().toString();
                    String getBio = snapshot.child("bio").getValue().toString();
                    String getNumber = snapshot.child("number").getValue().toString();
                    String getImage = snapshot.child("image").getValue().toString();

                    tvName.setText(getName);
                    tvBio.setText(getBio);
                    Picasso.get().load(getImage).placeholder(R.drawable.loading3).into(profileImage);

                } else {
                    String getName = snapshot.child("name").getValue().toString();
                    String getBio = snapshot.child("bio").getValue().toString();

                    tvName.setText(getName);
                    tvBio.setText(getBio);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}