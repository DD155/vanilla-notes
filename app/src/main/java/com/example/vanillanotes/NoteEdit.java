package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class NoteEdit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        String t = getIntent().getStringExtra("savedText");
        EditText text = findViewById(R.id.editText);
        text.setPadding(50, 50, 50, 50);

        if (t != null) { // case where user is editing old note
            text.setText(t); //set the text on the note page as the old string
            text.setSelection(text.getText().length()); //set cursor to the end
        }

        text.setBackgroundResource(R.drawable.back);
    }

    //Post-Condition: save the text of the note to the previous activity
    //case 1: user is creating new note: the key "savedText" should be null
    //case 2: user is editing a preexisting note: key "savedText" is not null and should use index
    public void saveText(View v){
        String t = getIntent().getStringExtra("savedText");
        Intent prev = new Intent();
        EditText text = findViewById(R.id.editText);
        prev.setClass(this, MainActivity.class);

        if (t == null) { // case where the note is new
            String s = text.getText().toString();
            prev.putExtra("note", s);
        } else { // case where the note is being edited
            ArrayList<String> list = getArrayList("textStrings");
            //replace old string with new string in the arraylist
            list.set(getIntent().getIntExtra("index", 0), text.getText().toString());
            saveArrayList(list, "textStrings");
        }
        startActivity(prev);
    }

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
}
