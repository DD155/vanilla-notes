package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
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

        TextView back = findViewById(R.id.toolbar_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity(MainActivity.class);
            }
        });

        if (t != null) { // case where user is editing old note
            text.setText(t); //set the text on the note page as the old string
            text.setSelection(text.getText().length()); //set cursor to the end
        }

        text.setBackgroundResource(R.drawable.back);
    }

    //Post-Condition: save the text of the note to the previous activity
    //case 1: user is creating new note: the key "savedText" should be null
    //case 2: user is editing a preexisting note: key "savedText" is not null and should use index
    public void saveText(){
        String t = getIntent().getStringExtra("savedText");
        Intent prev = new Intent();
        EditText text = findViewById(R.id.editText);
        prev.setClass(this, MainActivity.class);

        if (text.getText().length() == 0) { //check that the note is not empty
            warningDialog();
            return;
        }

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

        //load previously called activity
        if (caller.equals("TrashActivity")){
            goToActivity(TrashActivity.class);
        } else {
            goToActivity(MainActivity.class);
        }
    }

    public void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<String> trash, list;
        trash = getArrayList("trashStrings");
        list = getArrayList("textStrings");

        list.add(trash.get(index));
        trash.remove(index);

        saveArrayList(trash, "trashStrings");
        saveArrayList(list, "textStrings");

        goToActivity(MainActivity.class);
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
            getMenuInflater().inflate(R.menu.trash_actions, menu);
        } else
            getMenuInflater().inflate(R.menu.notes_actions, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                goToActivity(MainActivity.class);
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

    public void goToActivity(Class act){
        Intent i = new Intent();
        i.setClass(getApplicationContext(), act);
        startActivity(i);
    }
}
