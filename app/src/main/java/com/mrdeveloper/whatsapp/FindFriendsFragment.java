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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsFragment extends Fragment {

    MaterialToolbar toolbar;
    RecyclerView recyclerView;

    FirebaseAuth firebaseAuth;
    DatabaseReference userRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_find_friends, container, false);

        toolbar = myView.findViewById(R.id.toolBar);
        recyclerView = myView.findViewById(R.id.recyclerView);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return myView;

    } // ========================= End OnCreate ==============

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
                holder.tvName.setText(model.getName());
                holder.tvBio.setText(model.getBio());

                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.profileImage);

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

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friedns_item_layout,parent,false);

                return new FindFriendsViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }


    public class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvBio;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvBio = itemView.findViewById(R.id.tvBio);
            profileImage = itemView.findViewById(R.id.profileImage);

        }
    }

}