package com.sqsw.vanillanotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.Utility;
import com.sqsw.vanillanotes.nav_fragments.FavoritesFragment;
import com.sqsw.vanillanotes.nav_fragments.NoteFragment;
import com.sqsw.vanillanotes.nav_fragments.TrashFragment;
import com.sqsw.vanillanotes.nav_fragments.SettingsFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "NoteChannel";
    private final Utility UTIL = new Utility(this);
    private int selectedSortItem = 4;
    private boolean isTrashFrag;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel
        createNotificationChannel();

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Notes");
        setSupportActionBar(myToolbar);

        BottomNavigationView navView = findViewById(R.id.bottom_nav);
        navView.setItemIconTintList(null);

        navView.setOnNavigationItemSelectedListener(navListener);

        Log.d("fav_test", "Favorite extra: " + getIntent().getBooleanExtra("favorite", false));

        if (getIntent().getStringExtra("caller") != null) { // Start trash fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new TrashFragment()).commit();
            navView.getMenu().getItem(2).setChecked(true);
        } else if (getIntent().getBooleanExtra("favorite", false)){ // Start favs fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new FavoritesFragment()).commit();
            navView.getMenu().getItem(1).setChecked(true);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new NoteFragment()).commit();
            navView.getMenu().getItem(0).setChecked(true);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFrag = null;
                    switch (item.getItemId()){
                        case R.id.nav_notes:
                            //selectedFrag = new NoteFragment();
                            selectedFrag = new NoteFragment();
                            isTrashFrag = false;
                            break;

                        case R.id.nav_fav:
                            selectedFrag = new FavoritesFragment();
                            isTrashFrag = false;
                            break;

                        case R.id.nav_trash:
                            selectedFrag = new TrashFragment();
                            isTrashFrag = true;
                            break;

                        case R.id.nav_more:
                            isTrashFrag = false;
                            selectedFrag = new SettingsFragment();
                            break;
                    }

                    if (selectedFrag == null) selectedFrag = new NoteFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_layout, selectedFrag).commit();

                    return true;
                }
            };


    /*
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.hold_notes_actions, menu);
            mode.setTitle("Choose your notes");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //getSupportActionBar().hide();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                Toast.makeText(UTIL, "Clicked", Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            }
                return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };


    */


    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}

