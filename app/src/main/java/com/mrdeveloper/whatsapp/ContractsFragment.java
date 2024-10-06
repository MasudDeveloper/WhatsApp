package com.mrdeveloper.whatsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContractsFragment extends Fragment {

    MaterialToolbar toolbar;
    RecyclerView recyclerView;

    DatabaseReference contactsRef, userRef;
    FirebaseAuth firebaseAuth;
    String currentUserId, clickUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_contracts, container, false);

        toolbar = myView.findViewById(R.id.toolBar);
        recyclerView = myView.findViewById(R.id.recyclerView);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));




        return myView;

    } // ==================================== end ==========================

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {

                clickUserID = getRef(position).getKey();

                userRef.child(clickUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists() && snapshot.hasChild("image")) {

                            holder.tvName.setText(snapshot.child("name").getValue().toString());
                            holder.tvBio.setText(snapshot.child("bio").getValue().toString());

                            String getImage = snapshot.child("image").getValue().toString();

                            Picasso.get().load(getImage).placeholder(R.drawable.profile).into(holder.profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String clickUserID = getRef(position).getKey();
                                    Intent intent = new Intent(getContext(), UserProfileActivity.class);
                                    intent.putExtra("clickUserID",clickUserID);
                                    startActivity(intent);
                                }
                            });

                        } else {

                            holder.tvName.setText(snapshot.child("name").getValue().toString());
                            holder.tvBio.setText(snapshot.child("bio").getValue().toString());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String clickUserID = getRef(position).getKey();
                                    Intent intent = new Intent(getContext(), UserProfileActivity.class);
                                    intent.putExtra("clickUserID",clickUserID);
                                    startActivity(intent);
                                }
                            });

                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View myView = LayoutInflater.from(getContext()).inflate(R.layout.find_friedns_item_layout,parent,false);



                return new ContactsViewHolder(myView);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvBio;
        CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvBio = itemView.findViewById(R.id.tvBio);
            profileImage = itemView.findViewById(R.id.profileImage);

        }
    }

}