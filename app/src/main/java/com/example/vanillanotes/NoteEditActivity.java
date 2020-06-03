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


        if (t != null) { // case where user is editing old note
            text.setText(t); //set the text on the note page as the old string
            text.setSelection(text.getText().length()); //set cursor to the end
        }


        text.setBackgroundResource(R.drawable.shadow_border);
        title.setBackgroundResource(R.drawable.shadow_border);
    }

    //Post-Condition: save the text of the note to the previous activity
    //case 1: user is creating new note: the key "savedText" should be null
    //case 2: user is editing a preexisting note: key "savedText" is not null and should use index
    public void saveText(){
        ArrayList<String> list; //arraylist for either main notes or trash notes
        String key = "textStrings";
        Intent prev;
        boolean isTrash = false;
        String t = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        EditText text = findViewById(R.id.editText);

        //determine if previous activity was trash activity or not
        if (caller.equals("MainActivity")) prev = new Intent(getApplicationContext(), MainActivity.class);
        else {
            prev = new Intent(getApplicationContext(), TrashActivity.class);
            isTrash = true;
        }

        if (text.getText().length() == 0) { //check that the note is not empty
            warningDialog();
            return;
        }

        if (t == null) { // case where the note is new
            String s = text.getText().toString();
            prev.putExtra("note", s);
        } else { // case where the note is being edited
            //determine which list to use
            if (isTrash) {
                list = getArrayList("trashStrings");
                key = "trashStrings";
            }
            else list = getArrayList("textStrings");


            //replace old string with new string in the arraylist
            list.set(getIntent().getIntExtra("index", 0), text.getText().toString());
            saveArrayList(list, key);
        }
        startActivity(prev);
    }

    public void deleteNote(){
        String t = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        ArrayList<String> trashList;

        if (getArrayList("trashStrings") != null) // check if trash can list is valid
            trashList = getArrayList("trashStrings");
        else {
            trashList = new ArrayList<>();
        }
        ArrayList<String> list  = getArrayList("textStrings");


        if (t != null) { // save the note to trash while deleting from main notes
            if (caller.equals("TrashActivity")) {
                trashList.remove(getIntent().getIntExtra("index", 0)); //remove from trash can
            } else {
                trashList.add(t);
                list.remove(getIntent().getIntExtra("index", 0));
            }
            saveArrayList(list, "textStrings");
            saveArrayList(trashList, "trashStrings");
        }
        Toast.makeText(getApplicationContext(), "Note deleted", Toast.LENGTH_LONG).show();

        //load previously called activity
        if (caller.equals("TrashActivity")){
            goToActivity(TrashActivity.class);
        } else {
            goToActivity(MainActivity.class);
        }
    }

    //restore the note from the trash can to the main notes
    public void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<String> trash, list;
        trash = getArrayList("trashStrings");
        list = getArrayList("textStrings");

        list.add(trash.get(index));
        trash.remove(index);

        saveArrayList(trash, "trashStrings");
        saveArrayList(list, "textStrings");

        Toast.makeText(getApplicationContext(), "Note restored", Toast.LENGTH_LONG).show();
        //goToActivity(MainActivity.class);
    }

    //creates dialog for empty notes
    public void warningDialog(){
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

    public void confirmDialog(){
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
                        goToActivity(SettingsActivity.class);
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
                // notification function
                createNotification();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // saving array list functions
    public ArrayList<String> getArrayList(String key){ //returns the arraylist from sharedprefs
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveArrayList(ArrayList<String> list, String key){ // saves the arraylist using json
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public void goToActivity(Class<?> act){
        Intent i = new Intent();
        i.putExtra("caller", "NoteEditActivity");
        i.setClass(getApplicationContext(), act);
        startActivity(i);
    }

    public void createNotification(){
        EditText text = findViewById(R.id.editText);
        String message = text.getText().toString();
        /*
        String className = getIntent().getStringExtra("caller");
        Intent intent = new Intent(this, NoteEditActivity.class);

        intent.putExtra("savedText", "hello");
        intent.putExtra("caller", "MainActivity");
        intent.putExtra("index", 0);

         */
        // Create an Intent for the activity you want to start
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




        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NoteChannel")
                .setSmallIcon(R.drawable.ic_baseline_event_note_24)
                .setContentTitle("Pinned Note")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentText(message)
                .setContentIntent(resultPendingIntent)
                //.setContentText(text.getText().toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);



        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100, builder.build());
    }

}
