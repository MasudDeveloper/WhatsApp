package com.mrdeveloper.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UpdateProfile extends AppCompatActivity {

    ImageView imageView;
    Button pictureButton, updateButton;
    EditText edName, edBio, edNumber;

    FirebaseAuth myAuth;
    DatabaseReference dbRef;

    StorageReference storageRef, filePath;
    Uri uri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        pictureButton = findViewById(R.id.pictureButton);
        updateButton = findViewById(R.id.updateButton);
        edName = findViewById(R.id.edName);
        edBio = findViewById(R.id.edBio);
        edNumber = findViewById(R.id.edNumber);

        myAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        progressDialog = new ProgressDialog(UpdateProfile.this);


        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                ImagePicker.with(UpdateProfile.this)
                        .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                launcher.launch(intent);
                                return null;
                            }
                        });

            }
        });

        getAllDate();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edName.getText().toString();
                String bio = edBio.getText().toString();
                String number = edNumber.getText().toString();

                if (name.isEmpty()) {
                    edName.setError("Name is Empty");
                } else if (bio.isEmpty()) {
                    edBio.setError("Bio is Empty");
                } else if (number.isEmpty()) {
                    edNumber.setError("Number is Empty");
                } else {

                    String currentUID = myAuth.getCurrentUser().getUid();

                    HashMap<String ,String > hashMap = new HashMap<>();

                    hashMap.put("name",name);
                    hashMap.put("bio",bio);
                    hashMap.put("number",number);
                    hashMap.put("currentUID",currentUID);

                    dbRef.child("Users").child(currentUID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                progressDialog.setTitle("Updating Profile");
                                progressDialog.setMessage("Please Wait...");
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(UpdateProfile.this, "Data Update Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(UpdateProfile.this,MainActivity.class));
                                        finish();
                                    }
                                },2000);


                            }
                        }
                    });


                }

                String currentUserID = myAuth.getCurrentUser().getUid();

                filePath = storageRef.child(currentUserID+".jpg");

                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(UpdateProfile.this, "Profile Picture Uploaded", Toast.LENGTH_SHORT).show();

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageURL = uri.toString();

                                    dbRef.child("Users").child(currentUserID).child("image").setValue(imageURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(UpdateProfile.this, "Profile Sent to Database", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });


                        }
                    }
                });


            }
        });







    } // ======================= OnCreate End



    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = result.getData();
                uri = intent.getData();
                imageView.setImageURI(uri);

            }
        }
    });

    private void getAllDate() {

        String currentUID = myAuth.getCurrentUser().getUid();

        dbRef.child("Users").child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")) {
                    String getName = snapshot.child("name").getValue().toString();
                    String getBio = snapshot.child("bio").getValue().toString();
                    String getNumber = snapshot.child("number").getValue().toString();
                    String getImage = snapshot.child("image").getValue().toString();

                    edName.setText(getName);
                    edBio.setText(getBio);
                    edNumber.setText(getNumber);
                    Picasso.get().load(getImage).placeholder(R.drawable.loading3).into(imageView);


                } else if (snapshot.exists() && snapshot.hasChild("name")) {
                    String getName = snapshot.child("name").getValue().toString();
                    String getBio = snapshot.child("bio").getValue().toString();
                    String getNumber = snapshot.child("number").getValue().toString();

                    edName.setText(getName);
                    edBio.setText(getBio);
                    edNumber.setText(getNumber);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }



}