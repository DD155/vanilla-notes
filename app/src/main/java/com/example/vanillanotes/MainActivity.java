package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vanillanotes.settings.SettingsActivity;
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
        final Intent notesActivity = new Intent();

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
                        notesActivity.setClass(getApplicationContext(), NoteEditActivity.class);
                        notesActivity.putExtra("savedText", textList.get(finalI)); // pass current text
                        notesActivity.putExtra("index", finalI); // pass index to next activity to change content later
                        notesActivity.putExtra("caller", "MainActivity");
                        notesActivity.putExtra("class", MainActivity.class);
                        startActivity(notesActivity);
                    }
                });
            }
        }
    }

    //Post-condition: set attributes for text:
    //Text-size is now 15.
    //TextView now has border.
    //Change width and padding/margin accordingly
    public void initializeText(TextView text){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 25, 0, 25);
        text.setTextSize(15);
        text.setBackgroundResource(R.drawable.shadow_border);
        text.setWidth(1500);
        text.setPadding(30, 70, 30, 70);
        text.setLayoutParams(params);
        text.setTextColor(Color.parseColor("#434343"));

    }

    //Post-condition: Remove notes by clearing note arraylist and resetting linear layout.
    public void clearNotes(){
        ArrayList<String> list = getArrayList("textStrings");
        ArrayList<String> trash = getArrayList("trashStrings");
        trash.addAll(list);
        list.clear();

        saveArrayList(list, "textStrings");
        saveArrayList(trash, "trashStrings");
        //remove notes from layout
        LinearLayout ll = findViewById(R.id.linear);
        ll.removeAllViews();

        Toast.makeText(getApplicationContext(), "Notes cleared", Toast.LENGTH_LONG).show();

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
                goToActivity(SettingsActivity.class);
                return true;

            case R.id.action_add:

                goToActivity(NoteEditActivity.class);
                return true;

            case R.id.action_remove:
                goToActivity(TrashActivity.class);
                return true;

            case R.id.action_clear:
                createDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goToActivity(Class<?> act){
        Intent i = new Intent();
        i.putExtra("caller", "MainActivity");
        startActivity(i.setClass(getApplicationContext(), act));
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

    public int getNavigationBarSize(Context context){
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

}
