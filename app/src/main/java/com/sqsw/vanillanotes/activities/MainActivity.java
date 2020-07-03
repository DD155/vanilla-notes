package com.sqsw.vanillanotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sqsw.vanillanotes.classes.DateComparator;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.NoteComparator;
import com.sqsw.vanillanotes.classes.Utility;
import com.sqsw.vanillanotes.nav_fragments.NoteFragment;
import com.sqsw.vanillanotes.nav_fragments.TrashFragment;
import com.sqsw.vanillanotes.settings.SettingsFragmentCompat;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "NoteChannel";
    private final Utility UTIL = new Utility(this);
    private int selectedSortItem = 4;
    private DrawerLayout drawerLayout;

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

        final ArrayList<Note> noteList = UTIL.getNotes("notes");

        String editedText = getIntent().getStringExtra("note");
        String titleText = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        int color = getIntent().getIntExtra("color", 0);

        if (editedText != null){ // If the user has input text already, add new note with that text
            Note newNote = new Note(editedText);
            newNote.setDate(date);
            if (titleText != null) newNote.setTitle(titleText); // Check if there is a title
            if (color != -1) newNote.setColor(color);
            newNote.setStarred(getIntent().getBooleanExtra("star", false));
            // Add note to the top of the list
            noteList.add(0, newNote);

            UTIL.saveNotes(noteList, "notes");
        }

        BottomNavigationView navView = findViewById(R.id.bottom_nav);
        navView.setItemIconTintList(null);

        navView.setOnNavigationItemSelectedListener(navListener);

        if (getIntent().getStringExtra("caller") != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new TrashFragment()).commit();
            navView.getMenu().getItem(1).setChecked(true);
        } else {
            // Default fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new NoteFragment()).commit();
            navView.getMenu().getItem(0).setChecked(true);
        }


        /* Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_notes:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new NoteFragment(),
                                "notes_frag").commit();
                        break;

                    case R.id.nav_trash:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,
                                new TrashFragment(), "trash_frag").commit();
                        break;

                    case R.id.nav_fav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,
                                new FavoritesFragment()).commit();
                        break;

                    case R.id.nav_settings:
                        UTIL.goToActivity(SettingsActivity.class, "MainActivity", getApplicationContext());
                        break;

                    case R.id.nav_about:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DD155/vanilla-notes")));
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        if (getIntent().getStringExtra("caller") != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new TrashFragment()).commit();
            navigationView.getMenu().getItem(1).setChecked(true);
        } else {
            // Default fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new NoteFragment()).commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        // Set Navigation Button on Toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, myToolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

         */
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFrag = null;

                    switch (item.getItemId()){
                        case R.id.nav_notes:
                            selectedFrag = new NoteFragment();
                            break;

                        case R.id.nav_trash:
                            selectedFrag = new TrashFragment();
                            break;

                        case R.id.nav_more:
                            selectedFrag = new SettingsFragmentCompat();
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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
           sortDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    // Create dialog for sorting notes
    private void sortDialog() {
        final SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        if (selectedSortItem != prefs.getInt("sort_index", 0)){
            selectedSortItem = prefs.getInt("sort_index", 0);
        }

        String[] items = getResources().getStringArray(R.array.sort_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort");
        builder.setCancelable(true);
        builder.setSingleChoiceItems(items, selectedSortItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedSortItem = index;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("sort_index", index);
                editor.apply();
            }
        });

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("selected_index", selectedSortItem + "");
                dialogInterface.dismiss();
                sortNotes(selectedSortItem);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    // Function for sort based on user selection of previous dialog above
    // Type 0 = Sort by Title (Ascending)
    // Type 1 = Sort by Title (Descending)
    // Type 2 = Sort by Date Created (Ascending)
    // Type 3 = Sort by Date Created (Descending)
    // Type 4 = Custom Sort (User created sort)
    private void sortNotes(int type){
        ArrayList<Note> notes = UTIL.getNotes("notes");
        switch (type){
            case 0:
                Log.d("selected_index", "case 0");
                Collections.sort(notes, new NoteComparator());
                UTIL.saveNotes(notes, "notes");
                refreshActivity();
                break;
            case 1:
                Log.d("selected_index", "case 1");
                Collections.sort(notes, new NoteComparator());
                Collections.reverse(notes);
                UTIL.saveNotes(notes, "notes");
                refreshActivity();
                break;
            case 2:
                Collections.sort(notes, new DateComparator());
                UTIL.saveNotes(notes, "notes");
                refreshActivity();
                break;
            case 3:
                Collections.sort(notes, new DateComparator());
                Collections.reverse(notes);
                UTIL.saveNotes(notes, "notes");
                refreshActivity();
                break;

            case 4:
                // TODO: Custom sort
                break;
        }
    }

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

    private void refreshActivity(){
        finish();
        overridePendingTransition(0, 0);
        UTIL.goToActivity(MainActivity.class, null, this);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }
}

