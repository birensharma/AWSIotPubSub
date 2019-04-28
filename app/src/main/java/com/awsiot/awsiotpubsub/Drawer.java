package com.awsiot.awsiotpubsub;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Drawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final long FADE_DEFAULT_TIME = 100;
    private static final long MOVE_DEFAULT_TIME = 200;
    private FragmentManager fragmentManager;

    private Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        fragmentManager = getSupportFragmentManager();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        fragment=new Dashboard();
        setTitle("Dashboard");
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.dashboard) {
//            setTitle("Dashboard");
//            performTransition(new Dashboard(),fragment);
//
//        } else if (id == R.id.temp) {
//            setTitle("Temperature");
//            performTransition(new Temperature(),fragment);
//
//        } else if (id == R.id.humid) {
//            setTitle("Humidity");
//            performTransition(new Humidity(),fragment);
//        } else if (id == R.id.air) {
//            setTitle("Air Index");
//            performTransition(new AirIndex(),fragment);
//        }else if (id == R.id.facts) {
//            setTitle("Daily Fact");
//            performTransition(new Facts(),fragment);
//        }
//        else if (id == R.id.nav_share) {
//
//        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    private void performTransition(Fragment nextFragment, Fragment prevFragment)
//    {
//        if (isDestroyed())
//        {
//            return;
//        }
//        fragment=nextFragment;
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//        // 1. Exit for Previous Fragment
//        Fade exitFade = new Fade();
//        exitFade.setDuration(FADE_DEFAULT_TIME);
//        prevFragment.setExitTransition(exitFade);
//
//        // 2. Shared Elements Transition
//        TransitionSet enterTransitionSet = new TransitionSet();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            enterTransitionSet.addTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
//        }
//        enterTransitionSet.setDuration(MOVE_DEFAULT_TIME);
//        enterTransitionSet.setStartDelay(FADE_DEFAULT_TIME);
//        nextFragment.setSharedElementEnterTransition(enterTransitionSet);
//
//        // 3. Enter Transition for New Fragment
//        Fade enterFade = new Fade();
//        enterFade.setStartDelay(MOVE_DEFAULT_TIME);
//        enterFade.setDuration(FADE_DEFAULT_TIME);
//        nextFragment.setEnterTransition(enterFade);
//
//        fragmentTransaction.replace(R.id.content_frame, nextFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
}
