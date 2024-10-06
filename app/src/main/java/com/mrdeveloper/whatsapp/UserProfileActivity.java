package com.mrdeveloper.whatsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView tvName, tvBio;
    Button sendMessageButton, cancelRequestButton;

    FirebaseAuth firebaseAuth;
    DatabaseReference userRef, messageRequestRef, contactsRef;

    private String receiverUserId, senderUserID, currentStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileImage = findViewById(R.id.profile_image);
        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBio);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        cancelRequestButton = findViewById(R.id.cancelRequestButton);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messageRequestRef = FirebaseDatabase.getInstance().getReference().child("Message Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId = getIntent().getExtras().getString("clickUserID");
        senderUserID = firebaseAuth.getCurrentUser().getUid();
        currentStatus = "new";

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")) {

                    String getName = snapshot.child("name").getValue().toString();
                    String getBio = snapshot.child("bio").getValue().toString();
                    String getImage = snapshot.child("image").getValue().toString();

                    tvName.setText(getName);
                    tvBio.setText(getBio);
                    Picasso.get().load(getImage).placeholder(R.drawable.loading3).into(profileImage);

                    SentMessageRequestMain();

                } else {

                    String getName = snapshot.child("name").getValue().toString();
                    String getBio = snapshot.child("bio").getValue().toString();

                    tvName.setText(getName);
                    tvBio.setText(getBio);

                    SentMessageRequestMain();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    } // ================================= On Create End =====================
    

    private void SentMessageRequestMain() {

        messageRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(receiverUserId)) {

                    String requestType = snapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if (requestType.equals("sent")) {

                        currentStatus = "requestSent";
                        sendMessageButton.setText("Cancel Message Request");

                    } else if (requestType.equals("receive")){
                        currentStatus = "requestReceive";

                        sendMessageButton.setText("Approved Request");
                        cancelRequestButton.setVisibility(View.VISIBLE);
                        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelMessageRequest();
                            }
                        });


                    }

                } else {

                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserId)) {

                                String contacts = snapshot.child(receiverUserId).child("Contacts").getValue().toString();

                                if (contacts.equals("Saved")) {
                                    currentStatus = "friends";
                                    sendMessageButton.setText("Remove");
                                } else {
                                    currentStatus = "new";
                                    sendMessageButton.setText("Send Message");
                                }


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!senderUserID.equals(receiverUserId)) {

            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentStatus.equals("new")) {

                        sentMessageRequest();

                    } else if (currentStatus.equals("requestSent")) {

                        cancelMessageRequest();

                    } else if (currentStatus.equals("requestReceive")) {

                        acceptMessageRequest();

                    } else if (currentStatus.equals("friends")) {
                        removeMessageRequest();
                    }

                }
            });

        } else {

            sendMessageButton.setVisibility(View.INVISIBLE);

        }

    }


    private void sentMessageRequest() {

        messageRequestRef.child(senderUserID).child(receiverUserId)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            messageRequestRef.child(receiverUserId).child(senderUserID)
                                    .child("request_type").setValue("receive").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                currentStatus = "requestSent";
                                                sendMessageButton.setText("Cancel Message Request");
                                            }

                                        }
                                    });


                        }

                    }
                });

    }

    private void cancelMessageRequest() {

        messageRequestRef.child(senderUserID).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            messageRequestRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    currentStatus = "new";
                                    sendMessageButton.setText("Sent Message");
                                    cancelRequestButton.setVisibility(View.INVISIBLE);
                                }
                            });


                        }

                    }
                });


    }

    public void acceptMessageRequest() {

        contactsRef.child(senderUserID).child(receiverUserId)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(senderUserID)
                                    .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                messageRequestRef.child(senderUserID).child(receiverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    messageRequestRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            currentStatus = "friends";
                                                                            sendMessageButton.setText("Remove");
                                                                            cancelRequestButton.setVisibility(View.INVISIBLE);
                                                                        }
                                                                    });


                                                                }

                                                            }
                                                        });

                                            }

                                        }
                                    });


                        }

                    }
                });

    } // ======================================= end

    private void removeMessageRequest() {

        contactsRef.child(senderUserID).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            contactsRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    currentStatus = "new";
                                    sendMessageButton.setText("Sent Message");
                                    cancelRequestButton.setVisibility(View.INVISIBLE);
                                }
                            });


                        }

                    }
                });

    }


}