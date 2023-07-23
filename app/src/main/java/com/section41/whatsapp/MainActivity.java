package com.section41.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.section41.whatsapp.adapter.ViewPagerAdapter;
import com.section41.whatsapp.fragments.ChatsFragment;
import com.section41.whatsapp.fragments.ProfileFragment;
import com.section41.whatsapp.fragments.UsersFragment;
import com.section41.whatsapp.model.User;
import com.section41.whatsapp.registration.RegistrationKeyModel;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firebaseUser =FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser == null){
            // reload();
            Intent i = new Intent(MainActivity.this , LoginActivity.class);
            startActivity(i);
            finish();
        }
        myRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());



        //tab layout and viewPager
        TabLayout tableLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // add fragment
        viewPagerAdapter.addFragments(new ChatsFragment(),"Chats");
        viewPagerAdapter.addFragments(new UsersFragment(),"Users");
        viewPagerAdapter.addFragments(new ProfileFragment(),"Profile");
        //set adapter to our viewPager

        viewPager.setAdapter(viewPagerAdapter);
        tableLayout.setupWithViewPager(viewPager);




    }

    //adding logout functionality

    //add menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            //This flag ensures that when the user logs out, the app's navigation is reset
            //rather than being able to navigate back to previously visited screens using the back button.
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //method for status
    private void checkStatus(String status){
        myRef= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String ,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        myRef.updateChildren(hashMap);


    }

    @Override
    protected void onResume() {
        super.onResume();
    checkStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkStatus("Offline");

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null ||currentUser.isAnonymous()){
            // reload();
    Intent i = new Intent(MainActivity.this , LoginActivity.class);
        startActivity(i);
        finish();
        }

    }

    @Override
    protected void onStop() {

        super.onStop();

    }

}