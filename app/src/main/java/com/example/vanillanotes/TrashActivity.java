package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Trash");
        setSupportActionBar(myToolbar);

        final ArrayList<String> trashList; //declare arraylist for the strings of the text on each note
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent notesActivity = new Intent();

        if (prefs.contains("trashStrings")) { // checks if user has notes already
            Log.d("myTag", "trashStrings is valid.");
            trashList = getArrayList("trashStrings");
        } // otherwise just make the new arraylist
        else trashList = new ArrayList<>();

        // information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");

        if (editedText != null){ // if the user has input text already, add new note with that text
            trashList.add(editedText);
            saveArrayList(trashList, "trashStrings");
        }

        if (trashList.size() != 0){ // makes sure user has already notes, loads them on entering app
            for (int i = 0; i < trashList.size(); i++) {
                final TextView text = new TextView(this);
                Log.d("myTag", trashList.get(i));
                initializeText(text);
                text.setText(trashList.get(i));
                linear.addView(text);

                // make the text clickable
                final int finalI = i; //index of arraylist
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // clicked text sends user to edit the note
                        notesActivity.setClass(getApplicationContext(), NoteEdit.class);
                        notesActivity.putExtra("savedText", trashList.get(finalI)); // pass current text
                        notesActivity.putExtra("index", finalI); // pass index to next activity to change content later
                        notesActivity.putExtra("caller", "TrashActivity");
                        notesActivity.putExtra("class", TrashActivity.class);
                        startActivity(notesActivity);
                    }
                });
            }
        }
    }

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
}
