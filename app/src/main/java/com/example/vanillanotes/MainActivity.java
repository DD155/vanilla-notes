package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vanillanotes.settings.SettingsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "NoteChannel";
    private Utility util = new Utility(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel
        createNotificationChannel();

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Notes");
        setSupportActionBar(myToolbar);

        final ArrayList<Note> noteList; // Declare Notes ArrayList
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent notesActivity = new Intent();


        if (prefs.contains("notes")) { // Checks if user has notes already
            Log.d("myTag", "notes is valid.");
            noteList = util.getNotes("notes");
        } // Otherwise just make the new ArrayList
        else noteList = new ArrayList<>();

        // Information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");

        if (editedText != null){ // if the user has input text already, add new note with that text
            noteList.add(new Note("",editedText));
            util.saveNotes(noteList, "notes");
        }

        if (noteList.size() != 0){ // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(this);
                initializeText(text);
                text.setText(noteList.get(i).getText());
                linear.addView(text);

                // Make the text clickable
                final int index = i; // Index of ArrayList
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // clicked text sends user to edit the note
                        notesActivity.setClass(getApplicationContext(), NoteEditActivity.class);
                        notesActivity.putExtra("savedText", noteList.get(index).getText()); // pass current text
                        notesActivity.putExtra("index", index); // pass index to next activity to change content later
                        notesActivity.putExtra("caller", "MainActivity");
                        startActivity(notesActivity);
                    }
                });
            }
        }
    }

    // Set attributes for TextView depending on dpi
    private void initializeText(TextView text){
        float density = getResources().getDisplayMetrics().density;
        int height;
        Log.d("density", Float.toString(density));
        // Set height based on dpi
        if (density >= 4.0) {
            height = 300;
            Log.d("density", "Density is 4.0");
        } else if (density >= 3.0) {
            height = 250;
            Log.d("density", "Density is 3.0");
        } else if (density >= 2.0) {
            height = 150;
            Log.d("density", "Density is 2.0");

        } else if (density >= 1.5) {
            height = 100;
            Log.d("density", "Density is 1.5");
        } else
        {
            height = 75;
            Log.d("density", "Density is 1.0");
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 25, 0, 25);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        text.setBackgroundResource(R.drawable.shadow_border);
        text.setHeight(height);
        text.setPadding(30, 70, 30, 70);
        text.setLayoutParams(params);
        text.setTextColor(Color.parseColor("#434343"));
    }

    // Remove notes by clearing note ArrayList and resetting linear layout.
    private void clearNotes(){
        ArrayList<Note> list = util.getNotes("notes");
        ArrayList<Note> trash = util.getNotes("trash");
        trash.addAll(list);
        list.clear();

        util.saveNotes(list, "notes");
        util.saveNotes(trash, "trash");
        // Remove notes from layout
        LinearLayout ll = findViewById(R.id.linear);
        ll.removeAllViews();

        Toast.makeText(getApplicationContext(), "Notes cleared", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                util.goToActivity(SettingsActivity.class, "MainActivity", getApplicationContext());
                return true;

            case R.id.action_add:
                util.goToActivity(NoteEditActivity.class, "MainActivity", getApplicationContext());
                return true;

            case R.id.action_remove:
                util.goToActivity(TrashActivity.class,"MainActivity", getApplicationContext());
                return true;

            case R.id.action_clear:
                createDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Creates dialog for the clear
    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear All Notes");
        builder.setMessage("Would you like to clear all the notes?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearNotes();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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
}

