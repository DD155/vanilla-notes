package com.example.vanillanotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Notes");
        setSupportActionBar(myToolbar);

        final ArrayList<String> textList; //declare arraylist for the strings of the text on each note
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent goToSecond = new Intent();

        linear.getLayoutParams().height = 500;

        if (prefs.contains("textStrings")) { // checks if user has notes already
            Log.d("myTag", "textStrings is valid.");
            textList = getArrayList("textStrings");
        } // otherwise just make the new arraylist
        else textList = new ArrayList<>();

        // information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");

        if (editedText != null){ // if the user has input text already, add new note with that text
            textList.add(editedText);
            saveArrayList(textList, "textStrings");
        }

        if (textList.size() != 0){ // makes sure user has already notes, loads them on entering app
            for (int i = 0; i < textList.size(); i++) {
                final TextView text = new TextView(this);
                Log.d("myTag", textList.get(i));
                initializeText(text);
                text.setText(textList.get(i));
                linear.addView(text);

                // make the text clickable
                final int finalI = i; //index of arraylist
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // clicked text sends user to edit the note
                        goToSecond.setClass(getApplicationContext(), NoteEdit.class);
                        goToSecond.putExtra("savedText", textList.get(finalI)); // pass current text
                        goToSecond.putExtra("index", finalI); // pass index to next activity to change content later
                        startActivity(goToSecond);
                    }
                });
            }
        }
    }

    public static class ClearDialog extends AppCompatDialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Clear Notes");
            builder.setMessage("Would you like to clear all of your notes?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            return builder.create();
        }
    }

    //Post-condition: set attributes for text:
    //Text-size is now 15.
    //TextView now has border.
    //Change width and padding accordingly
    public void initializeText(TextView text){
        text.setTextSize(15);
        text.setBackgroundResource(R.drawable.back);
        text.setWidth(1500);
        text.setPadding(30, 70, 30, 70);
    }

    //Post-condition: Remove notes by clearing shared preferences and resetting linear layout.
    public void clearNotes(){
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        editor.clear();
        editor.apply();

        LinearLayout ll = findViewById(R.id.linear);
        ll.removeAllViews();

    }

    public void saveArrayList(ArrayList<String> list, String key){ // saves the arraylist using json
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<String> getArrayList(String key){ //returns the arraylist from sharedprefs
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
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
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_add:
                Intent notesActivity = new Intent();
                notesActivity.setClass(getApplicationContext(), NoteEdit.class);
                startActivity(notesActivity);
                return true;

            case R.id.action_remove:
                //clearNotes();
                createDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //creates dialog for the clear
    public void createDialog() {
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

    /*
    public void OpenClearDialog(){
        ClearDialog1 clear = new ClearDialog1();
        clear.show(getSupportFragmentManager(), "Clear Dialog");
    }*/



}
