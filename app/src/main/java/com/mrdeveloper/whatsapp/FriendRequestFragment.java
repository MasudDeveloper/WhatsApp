package com.mrdeveloper.whatsapp;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


public class FriendRequestFragment extends Fragment {

    RecyclerView recyclerView;

    FirebaseAuth firebaseAuth;
    DatabaseReference friendRequestRef,userRef, contactsRef,messageRequestRef;

    private String currentUserID, clickUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_friend_request, container, false);

        recyclerView = myView.findViewById(R.id.recyclerView);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Message Request").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        messageRequestRef = FirebaseDatabase.getInstance().getReference().child("Message Request");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return myView;
    } // ==========================================================

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(friendRequestRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FriendRequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FriendRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position, @NonNull Contacts model) {

                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.VISIBLE);

                clickUserID = getRef(position).getKey();

                DatabaseReference getKeyRef = getRef(position).child("request_type").getRef();

                getKeyRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            String type = snapshot.getValue().toString();

                            if (type.equals("receive")) {
                                userRef.child(clickUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists() && snapshot.hasChild("image") ) {

                                            String getImage = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(getImage).placeholder(R.drawable.profile).into(holder.profileImage);



                                        }

                                        holder.tvName.setText(snapshot.child("name").getValue().toString());
                                        holder.tvBio.setText(snapshot.child("bio").getValue().toString());

                                        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                acceptMessageRequest();
                                            }
                                        });

                                        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                removeMessageRequest();
                                            }
                                        });


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View myView = LayoutInflater.from(getContext()).inflate(R.layout.find_friedns_item_layout,parent,false);



                return new FriendRequestViewHolder(myView);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();


    } // ======================================

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView tvName, tvBio;
        Button acceptButton, rejectButton;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvBio = itemView.findViewById(R.id.tvBio);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);

        }
    } //====================================================

    public void acceptMessageRequest() {

        contactsRef.child(currentUserID).child(clickUserID)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            contactsRef.child(clickUserID).child(currentUserID)
                                    .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                messageRequestRef.child(currentUserID).child(clickUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    messageRequestRef.child(clickUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Toast.makeText(getContext(), "Contacts Added", Toast.LENGTH_SHORT).show();
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

    } // ======================================= end =====================

    private void removeMessageRequest() {

        contactsRef.child(currentUserID).child(clickUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            contactsRef.child(clickUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getContext(), "Request Cancel", Toast.LENGTH_SHORT).show();
                                }
                            });


                        }

                    }
                });

    }



}