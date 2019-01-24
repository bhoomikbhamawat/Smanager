package com.jain.shreyash.smanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.jain.shreyash.smanager.FragmentAttendance;
import com.jain.shreyash.smanager.FragmentOffline;
import com.jain.shreyash.smanager.FragmentOnline;
import com.jain.shreyash.smanager.R;

//implement the interface OnNavigationItemSelectedListener in your activity class
public class Dashboard extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //loading the default fragment
        loadFragment(new FragmentAttendance());

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.attendanceicon:
                fragment = new FragmentAttendance();
                break;

            case R.id.offlineicon:
                fragment = new FragmentOffline();
                break;

            case R.id.onlineicon:
                fragment = new FragmentOnline();
                break;


            case R.id.functionsicon:
                fragment = new FragmentFunctions();
                break;

        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }
}