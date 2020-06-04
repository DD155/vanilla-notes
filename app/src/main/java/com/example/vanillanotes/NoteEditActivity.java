package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vanillanotes.settings.SettingsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class NoteEditActivity extends AppCompatActivity {
    private Utility util = new Utility(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Edit");
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String t = getIntent().getStringExtra("savedText");
        EditText title = findViewById(R.id.titleText);
        EditText text = findViewById(R.id.editText);
        title.setPadding(50, 50, 50, 0);
        text.setPadding(50, 50, 50, 50);


        if (t != null) { // Case where user is editing old note
            text.setText(t); // Set the text on the note page as the old string
            text.setSelection(text.getText().length()); // Set cursor to the end
        }


        text.setBackgroundResource(R.drawable.shadow_border);
        title.setBackgroundResource(R.drawable.shadow_border);
    }

    // Save the text of the note to the previous activity
    private void saveText(){
        ArrayList<String> list; // ArrayList for either main notes or trash notes
        String key = "textStrings";
        Intent prev;
        boolean isTrash = false;
        String t = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        EditText text = findViewById(R.id.editText);

        // Determine if previous activity was trash activity or not
        if (caller.equals("MainActivity")) prev = new Intent(getApplicationContext(), MainActivity.class);
        else {
            prev = new Intent(getApplicationContext(), TrashActivity.class);
            isTrash = true;
        }

        if (text.getText().length() == 0) { // Check that the note is not empty
            warningDialog();
            return;
        }

        if (t == null) { // Case where the note is new
            String s = text.getText().toString();
            prev.putExtra("note", s);
        } else { // Case where the note is being edited
            // Determine which list to use
            if (isTrash) {
                list = util.getArrayList("trashStrings");
                key = "trashStrings";
            }
            else list = util.getArrayList("textStrings");

            // Replace old string with new string in the ArrayList
            list.set(getIntent().getIntExtra("index", 0), text.getText().toString());
            util.saveArrayList(list, key);
        }
        startActivity(prev);
    }

    private void deleteNote(){
        String t = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        ArrayList<String> trashList;

        if (util.getArrayList("trashStrings") != null) // check if trash can list is valid
            trashList = util.getArrayList("trashStrings");
        else {
            trashList = new ArrayList<>();
        }
        ArrayList<String> list  = util.getArrayList("textStrings");


        if (t != null) { // save the note to trash while deleting from main notes
            if (caller.equals("TrashActivity")) {
                trashList.remove(getIntent().getIntExtra("index", 0)); //remove from trash can
            } else {
                trashList.add(t);
                list.remove(getIntent().getIntExtra("index", 0));
            }
            util.saveArrayList(list, "textStrings");
            util.saveArrayList(trashList, "trashStrings");
        }
        Toast.makeText(getApplicationContext(), "Note deleted", Toast.LENGTH_LONG).show();

        // Load previously called activity
        if (caller.equals("TrashActivity")){
            util.goToActivity(TrashActivity.class, "NoteEditActivity", getApplicationContext());
        } else {
            util.goToActivity(MainActivity.class, "NoteEditActivity", getApplicationContext());
        }
    }

    // Restore the note from the trash can to the main notes
    private void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<String> trash, list;
        trash = util.getArrayList("trashStrings");
        list = util.getArrayList("textStrings");

        list.add(trash.get(index));
        trash.remove(index);

        util.saveArrayList(trash, "trashStrings");
        util.saveArrayList(list, "textStrings");

        Toast.makeText(getApplicationContext(), "Note restored", Toast.LENGTH_LONG).show();
    }

    // Creates dialog for empty notes
    private void warningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Your note is currently blank. Please enter text to save it.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Note");
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote();
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

    //functions for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getStringExtra("caller").equals("TrashActivity")) {
            getMenuInflater().inflate(R.menu.trash_note_actions, menu);
        } else
            getMenuInflater().inflate(R.menu.notes_actions, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                if (getIntent().getStringExtra("caller").equals("MainActivity"))
                    intent.setClass(getApplicationContext(), MainActivity.class);
                else
                    intent.setClass(getApplicationContext(), TrashActivity.class);

                startActivityForResult(intent, 0);
                return true;

            case R.id.action_settings:
                // Create dialog if user wants to continue to settings, discarding the current note.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Discard changes");
                builder.setMessage("Are you sure you want to discard your changes?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        util.goToActivity(SettingsActivity.class,"NoteEditActivity", getApplicationContext());
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;

            case R.id.action_save:
                saveText();
                return true;

            case R.id.action_restore:
                restoreNote();
                return true;

            case R.id.action_delete:
                confirmDialog();
                return true;

            case R.id.action_pin:
                createNotification();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createNotification(){
        EditText text = findViewById(R.id.editText);
        String message = text.getText().toString();

        // Create an Intent for the activity
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("savedText", message);
        intent.putExtra("caller", getIntent().getStringExtra("caller"));
        intent.putExtra("index", getIntent().getIntExtra("index", 0));
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NoteChannel")
                .setSmallIcon(R.drawable.ic_baseline_event_note_24)
                .setContentTitle("Pinned Note")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentText(message)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100, builder.build());
    }
}
