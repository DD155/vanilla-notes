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
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.Utility;
import com.sqsw.vanillanotes.fragments.FavoritesFragment;
import com.sqsw.vanillanotes.fragments.NoteFragment;
import com.sqsw.vanillanotes.fragments.TrashFragment;
import com.sqsw.vanillanotes.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "NoteChannel";
    private final Utility UTIL = new Utility(this);
    private Context mContext;
    private int ctr = 0;
    private int selectedSortItem = 4;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        // Create notification channel
        createNotificationChannel();

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Notes");
        setSupportActionBar(myToolbar);

        BottomNavigationView navView = findViewById(R.id.bottom_nav);
        navView.setItemIconTintList(null);

        navView.setOnNavigationItemSelectedListener(navListener);

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

        FloatingActionButton fabNote = findViewById(R.id.fab_item_note);
        fabNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UTIL.goToActivity(NoteEditActivity.class, null, mContext);
            }
        });


        FloatingActionButton fabChecklist = findViewById(R.id.fab_item_checklist);

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
                            break;

                        case R.id.nav_fav:
                            selectedFrag = new FavoritesFragment();
                            break;

                        case R.id.nav_trash:
                            selectedFrag = new TrashFragment();
                            break;
                        case R.id.nav_more:
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
        if (ctr > 0) {
            super.onBackPressed();
        } else {
            ctr++;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }
}

