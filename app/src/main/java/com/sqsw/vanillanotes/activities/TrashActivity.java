package com.sqsw.vanillanotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.Utility;
import com.sqsw.vanillanotes.settings.SettingsActivity;

import java.util.ArrayList;

public class TrashActivity extends AppCompatActivity {
    private final Utility UTIL = new Utility(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_layout);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Trash");
        setSupportActionBar(myToolbar);

        final ArrayList<Note> noteList; // Declare Notes ArrayList
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent notesActivity = new Intent();

        Log.d("trash", "clear 1");

        if (prefs.contains("trash")) { // Checks if user has notes already
            noteList = UTIL.getNotes("trash");
        } // Otherwise just make the new ArrayList
        else noteList = new ArrayList<>();

        Log.d("trash", "clear 2");

        // Information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");
        final String titleText = caller.getStringExtra("title");
        final String date = caller.getStringExtra("date");
        final int color = caller.getIntExtra("color", 0);
        if (date != null)
            Log.d("date_log", date);

        Log.d("trash", "clear 3");

        if (editedText != null){ // If the user has input text already, add new note with that text
            Note newNote = new Note(editedText);
            newNote.setDate(date);
            if (titleText != null) newNote.setTitle(titleText); // Check if there is a title
            if (color != -1) newNote.setColor(color);
            noteList.add(newNote);

            UTIL.saveNotes(noteList, "trash");
        }

        Log.d("trash", "clear 4");

        if (noteList.size() != 0){ // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(this);
                final Note currNote = noteList.get(i);
                String title = currNote.getTitle();
                String description = currNote.getText();
                Log.d("color_picked", ""+currNote.getColor());
                Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, currNote.getColor());
                text.setBackground(drawable);
                /*
                String[] strParts = description.split("\\r?\\n|\\r");

                Log.d("length", Integer.toString(strParts[0].length()));
                if (strParts[0].length() >= 82){
                    description = util.addEllipsis(strParts[0]);
                    Log.d("length", Integer.toString(description.length()));
                } else if (util.countLines(description) > 2){
                    // Create array of the text without any new lines
                    description = strParts[0] + "\n" + util.addEllipsis(strParts[1]); // Concatenate everything
                }
                 */

                // Make the title larger than the description
                Log.d("trash", title);
                SpannableString str = new SpannableString(title + "\n" + description);

                str.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                initializeText(text);
                text.setText(str);
                linear.addView(text);

                // Make the text clickable
                final int index = i; // Index of ArrayList
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // clicked text sends user to edit the note
                        notesActivity.setClass(getApplicationContext(), NoteEditActivity.class);
                        notesActivity.putExtra("savedText", noteList.get(index).getText()); // pass current text
                        notesActivity.putExtra("savedTitle", noteList.get(index).getTitle());
                        notesActivity.putExtra("index", index); // pass index to next activity to change content later
                        notesActivity.putExtra("caller", "TrashActivity");
                        notesActivity.putExtra("date", noteList.get(index).getDate());
                        notesActivity.putExtra("color", currNote.getColor());
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
        float density = getResources().getDisplayMetrics().density;
        int fontSize = UTIL.getFontSize(getSharedPreferences("NOTES", Context.MODE_PRIVATE).getString("font_size", ""));
        int height;
        Log.d("density", Float.toString(density));
        // Set height based on dpi
        if (density >= 4.0) {
            height = 350;
            Log.d("density", "Density is 4.0");
        } else if (density >= 3.0) {
            height = 300;
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
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        text.setFilters(new InputFilter[] { new InputFilter.LengthFilter(82) });
        //text.setBackgroundResource(R.drawable.shadow_border);
        text.setHeight(height);
        text.setPadding(50, 20, 50, 30);
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
            case R.id.action_empty:
                if (UTIL.getNotes("trash").size() != 0)
                    confirmDialog();
                else {  // Case where the trash is already empty
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.clear_error_title);
                    builder.setMessage(getResources().getString(R.string.trash_error));
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           //builder.create().dismiss();
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
        ArrayList<Note> list = UTIL.getNotes("trash");
        list.clear();
        UTIL.saveNotes(list, "trash");

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
