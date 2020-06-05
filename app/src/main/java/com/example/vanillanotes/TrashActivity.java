package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vanillanotes.settings.SettingsActivity;

import java.util.ArrayList;

public class TrashActivity extends AppCompatActivity {
    private Utility utility = new Utility(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Trash");
        setSupportActionBar(myToolbar);

        final ArrayList<Note> noteList; // Declare Notes ArrayList
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent notesActivity = new Intent();


        if (prefs.contains("trash")) { // Checks if user has notes already
            noteList = utility.getNotes("trash");
        } // Otherwise just make the new ArrayList
        else noteList = new ArrayList<>();

        // Information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");

        if (editedText != null){ // if the user has input text already, add new note with that text
            noteList.add(new Note("",editedText));
            utility.saveNotes(noteList, "trash");
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
                        notesActivity.putExtra("caller", "TrashActivity");
                        startActivity(notesActivity);
                    }
                });
            }
        } else { // Have message that trash can is empty
            TextView defaultText = new TextView(getApplicationContext());
            defaultText.setText(getResources().getString(R.string.trash_empty));
            defaultText.setTextSize(20);
            defaultText.setGravity(Gravity.CENTER_HORIZONTAL);
            linear.addView(defaultText);
        }
    }

    private void initializeText(TextView text){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 25, 0, 25);
        text.setTextSize(15);
        text.setBackgroundResource(R.drawable.shadow_border);
        text.setWidth(1500);
        text.setPadding(30, 70, 30, 70);
        text.setLayoutParams(params);
        text.setTextColor(Color.parseColor("#434343"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trash_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                utility.goToActivity(SettingsActivity.class, "TrashActivity", getApplicationContext());
                return true;

            case R.id.action_home:
                utility.goToActivity(MainActivity.class, "TrashActivity", getApplicationContext());
                return true;

            case R.id.action_empty:
                if (utility.getNotes("trash").size() != 0)
                    confirmDialog();
                else {  // Case where the trash is already empty
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.clear_error_title);
                    builder.setMessage(getResources().getString(R.string.trash_error));
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Empty Trash");
        builder.setMessage(getResources().getString(R.string.trash_clear_confirm));
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

    private void clearNotes(){
        ArrayList<Note> list = utility.getNotes("trash");
        list.clear();
        utility.saveNotes(list, "trash");

        LinearLayout ll = findViewById(R.id.linear);
        ll.removeAllViews();

        TextView defaultText = new TextView(getApplicationContext());
        defaultText.setText(getResources().getString(R.string.trash_empty));
        defaultText.setTextSize(20);
        defaultText.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(defaultText);

        Toast.makeText(getApplicationContext(), "Trash emptied", Toast.LENGTH_LONG).show();
    }
}
