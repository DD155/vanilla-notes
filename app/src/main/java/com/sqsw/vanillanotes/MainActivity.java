package com.sqsw.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sqsw.vanillanotes.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    //private GestureDetector detector;
    private static final String CHANNEL_ID = "NoteChannel";
    private final Utility UTIL = new Utility(this);
    private int selectedSortItem = 0;
    //private ArrayList<Note> deleteNotes;
    //private ArrayAdapter<Note> adapter;
    //private ActionMode actionMode;
    //private boolean isHeld = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create notification channel
        createNotificationChannel();

        sortByTitle();

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Notes");
        setSupportActionBar(myToolbar);

        final ArrayList<Note> noteList; // Declare Notes ArrayList
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent notesActivity = new Intent();


        if (prefs.contains("notes")) { // Checks if user has notes already
            noteList = UTIL.getNotes("notes");
        } // Otherwise just make the new ArrayList
        else noteList = new ArrayList<>();

        // Information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");
        final String titleText = caller.getStringExtra("title");
        final String date = caller.getStringExtra("date");
        final int color = caller.getIntExtra("color", 0);
        if (date != null)
        Log.d("date_log", date);

        if (editedText != null){ // If the user has input text already, add new note with that text
            Note newNote = new Note(editedText);
            newNote.setDate(date);
            if (titleText != null) newNote.setTitle(titleText); // Check if there is a title
            if (color != -1) newNote.setColor(color);
            noteList.add(newNote);

            UTIL.saveNotes(noteList, "notes");
        }

        if (noteList.size() != 0){ // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(this);
                final Note currNote = noteList.get(i);
                String title = currNote.getTitle();
                String description = currNote.getText();
                Log.d("color_picked", ""+currNote.getColor());
                final Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, currNote.getColor());
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
                SpannableString str = new SpannableString(title + "\n" + description);
                str.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                initializeText(text, currNote.getColor());
                text.setText(str);
                linear.addView(text);

                // Make the text clickable
                final int index = i; // Index of ArrayList

                text.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()){
                            case MotionEvent.ACTION_CANCEL:
                                Log.d("cancel_action", "Cancel action");
                                text.setBackground(drawable);
                                return true;

                            case MotionEvent.ACTION_DOWN:
                                if (currNote.getColor() != -1) {
                                    // Logic for making pressed down color a darker shade
                                    String[] rgbStr = {(UTIL.hexFromColorInt(currNote.getColor())).substring(0, 2),
                                            (UTIL.hexFromColorInt(currNote.getColor())).substring(2, 4),
                                            (UTIL.hexFromColorInt(currNote.getColor())).substring(4)
                                    };
                                    double[] rgb = { // Divide RGB value to make the result darker
                                            Math.round(Integer.valueOf(rgbStr[0], 16) * 0.75),
                                            Math.round(Integer.valueOf(rgbStr[1], 16) * 0.75),
                                            Math.round(Integer.valueOf(rgbStr[2], 16) * 0.75)
                                    };
                                    // Format string in #RRGGBB style
                                    String newHex = String.format("#%02X%02X%02X", (int)rgb[0], (int)rgb[1], (int)rgb[2]);

                                    // Create new drawable to replace
                                    Drawable holdDrawable = UTIL.returnDrawable(R.drawable.shadow_border);
                                    holdDrawable.setColorFilter(new
                                            PorterDuffColorFilter(Color.parseColor(newHex), PorterDuff.Mode.MULTIPLY));

                                    text.setBackground(holdDrawable);

                                    Log.d("shade", "New Hex: " + newHex);
                                } else
                                    text.setBackgroundResource(R.drawable.shadow_border_hold);

                                return true;

                            case MotionEvent.ACTION_UP:
                                // Check if location of user touch is still within the TextView
                                if ((int)event.getX() >= 0 && (int)event.getX() <= 1360
                                        && (int)event.getY() >= 0 && (int)event.getY() <= 300){
                                    notesActivity.setClass(getApplicationContext(), NoteEditActivity.class);
                                    notesActivity.putExtra("savedText", noteList.get(index).getText()); // pass current text
                                    notesActivity.putExtra("savedTitle", noteList.get(index).getTitle());
                                    notesActivity.putExtra("index", index); // pass index to next activity to change content later
                                    notesActivity.putExtra("caller", "MainActivity");
                                    notesActivity.putExtra("date", noteList.get(index).getDate());
                                    notesActivity.putExtra("color", currNote.getColor());
                                    text.setBackground(drawable);
                                    startActivity(notesActivity);
                                } else {
                                    // Just change the color back if user moves finger out of the textbox
                                    text.setBackground(drawable);
                                }
                                return true;
                        }
                        return false;
                    }
                });

                /*
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // clicked text sends user to edit the note
                        notesActivity.setClass(getApplicationContext(), NoteEditActivity.class);
                        notesActivity.putExtra("savedText", noteList.get(index).getText()); // pass current text
                        notesActivity.putExtra("savedTitle", noteList.get(index).getTitle());
                        notesActivity.putExtra("index", index); // pass index to next activity to change content later
                        notesActivity.putExtra("caller", "MainActivity");
                        notesActivity.putExtra("date", noteList.get(index).getDate());
                        startActivity(notesActivity);
                    }
                });

                text.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (actionMode != null){
                            return false;
                        }
                        actionMode = startSupportActionMode(callback);

                        return true;
                    }
                });*/
            }
        }
    }

    /*
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.hold_notes_actions, menu);
            mode.setTitle("Choose your notes");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //getSupportActionBar().hide();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                Toast.makeText(UTIL, "Clicked", Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            }
                return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }; */


    // Set attributes for TextView depending on dpi
    private void initializeText(TextView text, int color){
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
        if (UTIL.isDarkColor(color))
            text.setTextColor(getResources().getColor(R.color.white));
        else text.setTextColor(getResources().getColor(R.color.textColor));
    }

    // Remove notes by clearing note ArrayList and resetting linear layout.
    private void clearNotes(){
        ArrayList<Note> list = UTIL.getNotes("notes");
        ArrayList<Note> trash = UTIL.getNotes("trash");
        trash.addAll(list);
        list.clear();

        UTIL.saveNotes(list, "notes");
        UTIL.saveNotes(trash, "trash");
        // Remove notes from layout
        LinearLayout ll = findViewById(R.id.linear);
        ll.removeAllViews();

        Toast.makeText(getApplicationContext(), getString(R.string.clear_notes_toast), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //if (isHeld)
        //    getMenuInflater().inflate(R.menu.hold_notes_actions, menu);
        //else
            getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                UTIL.goToActivity(SettingsActivity.class, "MainActivity", getApplicationContext());
                return true;

            case R.id.action_add:
                UTIL.goToActivity(NoteEditActivity.class, "MainActivity", getApplicationContext());
                return true;

            case R.id.action_remove:
                UTIL.goToActivity(TrashActivity.class,"MainActivity", getApplicationContext());
                return true;

            case R.id.action_sort:
                sortDialog();
                return true;

            case R.id.action_clear:
                createDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortDialog() {
        String[] items = getResources().getStringArray(R.array.sort_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort");
        builder.setCancelable(true);
        builder.setSingleChoiceItems(items, selectedSortItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedSortItem = index;
            }
        });

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("selected_index", selectedSortItem + "");
                dialogInterface.dismiss();
                sortNotes(selectedSortItem);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    // Function for sort based on user selection of previous dialog above
    // Type 0 = Sort by Title (Ascending)
    // Type 1 = Sort by Title (Descending)
    // Type 2 = Sort by Date Created (Ascending)
    // Type 3 = Sort by Date Created (Descending)
    // Type 4 = Custom Sort (User created sort)
    private void sortNotes(int type){
        ArrayList<Note> notes = UTIL.getNotes("notes");
        switch (type){
            case 0:
                Log.d("selected_index", "case 0");
                Collections.sort(notes, new NoteComparator());
                UTIL.saveNotes(notes, "notes");
                refreshActivity();
                break;
            case 1:
                Log.d("selected_index", "case 1");
                Collections.sort(notes, new NoteComparator());
                Collections.reverse(notes);
                UTIL.saveNotes(notes, "notes");
                refreshActivity();
                break;
            case 2:

                break;

            case 3:

                break;

            case 4:

                break;
        }
    }

    private void sortByTitle(){
        /*
        ArrayList<Note> notes = UTIL.getNotes("notes");
        for (int i = 0; i < notes.size(); i++) Log.d("sort_test", "Unsorted: " + notes.get(i).getTitle());

        Collections.sort(notes, new SortTitleAscending());

        for (int i = 0; i < notes.size(); i++) Log.d("sort_test", "Sorted: " + notes.get(i).getTitle());
           */
    }

    // Creates dialog for the clear
    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.clear_notes_title));
        builder.setMessage(getString(R.string.clear_notes_text));
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

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void refreshActivity(){
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
}

