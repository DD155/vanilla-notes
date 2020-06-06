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

import org.w3c.dom.Text;

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

        TextView dateView = findViewById(R.id.date);

        // Set attributes of EditTexts
        String text = getIntent().getStringExtra("savedText");
        String title = getIntent().getStringExtra("savedTitle");
        EditText titleView = findViewById(R.id.titleText);
        EditText textView = findViewById(R.id.editText);
        Log.d("date_time_0", util.currentDate());
        titleView.setPadding(50, 50, 50, 0);
        textView.setPadding(50, 50, 50, 50);

        if (text != null) { // Case where user is editing old note
            Note currentNote = util.getNotes("notes")
                    .get(getIntent().getIntExtra("index", 0));
            dateView.setText(getString(R.string.date_created, currentNote.getDate()));
            //dateView.setText("Date Created: " + currentNote.getDate());
            if (title != null) titleView.setText(title);
            textView.setText(text); // Set the text on the note page as the old string
            textView.setSelection(textView.getText().length()); // Set cursor to the end
            textView.requestFocus(); // Set cursor to this View specifically
        } else {
            dateView.setText(getString(R.string.date_created, util.currentDate()));
        }

        textView.setBackgroundResource(R.drawable.shadow_border);
        titleView.setBackgroundResource(R.drawable.shadow_border);

    }

    // Save the text of the note to the previous activity
    private void saveText(){
        ArrayList<Note> list; // ArrayList for either main notes or trash notes
        String key = "notes";
        Intent prev;
        boolean isTrash = false;
        String text = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        EditText textView = findViewById(R.id.editText);
        EditText titleView = findViewById(R.id.titleText);
        // Determine if previous activity was trash activity or not
        if (caller.equals("MainActivity")) prev = new Intent(getApplicationContext(), MainActivity.class);
        else {
            prev = new Intent(getApplicationContext(), TrashActivity.class);
            isTrash = true;
        }

        if (textView.getText().length() == 0) { // Check that the note is not empty
            warningDialog();
            return;
        }

        if (text == null) { // Case where the note is new
            prev.putExtra("note", textView.getText().toString());
            prev.putExtra("title", titleView.getText().toString());
            prev.putExtra("date", util.currentDate());
        } else { // Case where the note is being edited
            // Determine which list to use
            if (isTrash) {
                list = util.getNotes("trash");
                key = "trash";
            }
            else list = util.getNotes("notes");

            // Replace old strings with new strings in the ArrayList
            list.get(getIntent().getIntExtra("index", 0)).setText(textView.getText().toString());
            list.get(getIntent().getIntExtra("index", 0)).setTitle(titleView.getText().toString());
            util.saveNotes(list, key);
        }
        startActivity(prev);
    }

    private void deleteNote(){
        String text = getIntent().getStringExtra("savedText");
        String title = getIntent().getStringExtra("savedTitle");
        String caller = getIntent().getStringExtra("caller");
        ArrayList<Note> trashList;

        if (util.getNotes("trash") != null) // check if trash can list is valid
            trashList = util.getNotes("trash");
        else {
            trashList = new ArrayList<>();
        }
        ArrayList<Note> list  = util.getNotes("notes");


        if (text != null) { // save the note to trash while deleting from main notes
            if (caller.equals("TrashActivity")) {
                trashList.remove(getIntent().getIntExtra("index", 0)); //remove from trash can
            } else {
                trashList.add(new Note(title, text));
                list.remove(getIntent().getIntExtra("index", 0));
            }
            util.saveNotes(list, "notes");
            util.saveNotes(trashList, "trash");
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
        ArrayList<Note> trash, list;
        trash = util.getNotes("trash");
        list = util.getNotes("notes");

        list.add(trash.get(index));
        trash.remove(index);

        util.saveNotes(trash, "trash");
        util.saveNotes(list, "notes");

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
