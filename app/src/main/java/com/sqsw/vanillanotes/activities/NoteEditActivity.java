package com.sqsw.vanillanotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;
import petrov.kristiyan.colorpicker.ColorPicker;
//import eltos.simpledialogfragment.color.SimpleColorDialog;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sqsw.vanillanotes.classes.BroadcastReminder;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NoteEditActivity extends AppCompatActivity {
    private final Utility UTIL = new Utility(this);
    private SharedPreferences prefs; // Used for confirming discard dialog
    private Context mContext = this;
    private Menu mMenu;

    private EditText contentView;
    private EditText titleView;

    private int colorPicked = -1;

    private boolean isOldNote; // Determines if the user is editing an old note or not
    private boolean isTrash = false; // Determines if user clicked on note from trash fragment
    private boolean isFavorite; // Coming from a previously favorited note
    private boolean newFavorite; // The new favorite value based on user changing it in this activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit_layout);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView dateView = findViewById(R.id.date);
        titleView = findViewById(R.id.titleText);
        contentView = findViewById(R.id.editText);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int fontSize = UTIL.getFontSize(getSharedPreferences("NOTES", Context.MODE_PRIVATE)
                .getString("font_size", ""));

        // Check previous activity's extras
        isTrash = "Trash".equals(getIntent().getStringExtra("caller"));
        isOldNote = getIntent().getBooleanExtra("oldNote", false);
        isFavorite = getIntent().getBooleanExtra("favorite", false);

        // Set newFavorite as the value of isFavorite first so the toggle works correctly
        newFavorite = isFavorite;

        // Set attributes of EditTexts
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSize + 5));
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        contentView.setScrollY(0);

        if (isOldNote) { // Case where user is editing old note
            Note currentNote = getCurrentNote();
            // Set date information; uses substring because original date string includes seconds
            colorPicked = currentNote.getColor();
            String dateString = currentNote.getDate().substring(0, currentNote.getDate().length() - 6)
                    + " " + currentNote.getDate().substring(currentNote.getDate().length() - 2);
            dateView.setText(getString(R.string.date_created, dateString));

            // Set content and title
            titleView.setText(currentNote.getTitle());
            contentView.setText(currentNote.getText()); // Set the text on the note page as the old string

            // Set color
            colorPicked = currentNote.getColor();
            CardView cv = findViewById(R.id.card);
            cv.setCardBackgroundColor(colorPicked);

            // Set text color depending if color is dark or not
            if (UTIL.isDarkColor(colorPicked)) {
                titleView.setTextColor(getResources().getColor(R.color.white));
                contentView.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    // Retrive ArrayList depending on if user entered from activity trash or home
    private Note getCurrentNote(){
        Note currentNote;
        if (isTrash)
            currentNote = UTIL.getNotes("trash")
                    .get(getIntent().getIntExtra("index", 0));
        else if (isFavorite)
            currentNote = UTIL.getNotes("favorites")
                .get(getIntent().getIntExtra("index", 0));
        else {
            currentNote = UTIL.getNotes("notes")
                    .get(getIntent().getIntExtra("index", 0));
        }
        return currentNote;
    }

    // Helper function for getting the correct notes depending on the user's previous fragment
    private String returnKeyFromList(Intent intent){
        if (isTrash) {
            intent.putExtra("caller", "Trash");
            return "trash";
        } else if (isFavorite){
            intent.putExtra("favorite", true);
            return "favorites";
        } else return "notes";
    }

    // Case 1: If the note is NOT originally a favorited note and is now being favorited by the user
    // Case 2: If the note was a favorited note and the user wants to unfavorite it now
    private void saveToFavorites(ArrayList<Note> list, int index){
        Note current = list.get(index);
        // If the note is newly favorited, remove it from its current list and add to favorites list
        if (!isFavorite && newFavorite) {
            ArrayList<Note> fav = UTIL.getNotes("favorites");
            Log.d("fav_test", "Current index: " + index);
            fav.add(0, current);
            UTIL.saveNotes(fav, "favorites");
            list.remove(index);
        } else if (isFavorite && !newFavorite){
            // Case where note was originally in favorites, but user wants to unfavorite
            // So put it back in the note list and remove from the favorite list
            ArrayList<Note> notes = UTIL.getNotes("notes");
            notes.add(0, current);
            UTIL.saveNotes(notes, "notes");
            list.remove(index);
        } else {
            list.remove(index);
            list.add(0, current);
        }
    }

    // Save the text of the note to the previous activity
    private void saveText(){
        Intent intent = new Intent(this, MainActivity.class);
        String contentText = contentView.getText().toString().trim();
        String titleText = titleView.getText().toString().trim();
        String key = returnKeyFromList(intent);

        if (contentView.length() == 0) { // Check that the note is not empty
            warningDialog();
            return;
        }

        ArrayList<Note> list = UTIL.getNotes(key);

        if (isOldNote) { // Case where the note is old
            int index = getIntent().getIntExtra("index", 0);
            Log.d("fav_test", "Current index: " + index);
            Note current = list.get(index);
            // Set new attributes to the note
            if (colorPicked != -1) current.setColor(colorPicked);
            current.setText(contentText);
            current.setTitle(titleText);
            current.setFavorite(newFavorite);
            /* Move the old saved note to the top
            if (index != 0) {
                list.remove(index);
                list.add(0, current);
            }*/

            saveToFavorites(list, index);
            UTIL.saveNotes(list, key);
        } else {
            Note newNote = new Note(titleText, contentText, colorPicked, UTIL.currentDate());
            newNote.setFavorite(newFavorite);
            // If new note is favorited, add it to the favorites list
            if (newFavorite) {
                ArrayList<Note> fav = UTIL.getNotes("favorites");
                fav.add(0, newNote);
                UTIL.saveNotes(fav, "favorites");
            } else {
                list.add(0, newNote);
                UTIL.saveNotes(list, key);
            }
        }
        startActivity(intent);
    }
    private void deleteNote(){
        if (!isOldNote){
            UTIL.goToActivity(MainActivity.class, null, getApplicationContext());
            return;
        }

        String key;
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<Note> trashList = UTIL.getNotes("trash");
        ArrayList<Note> list;

        if (isFavorite) {
            list = UTIL.getNotes("favorites");
            key = "favorites";
        }
        else {
            list = UTIL.getNotes("notes");
            key = "notes";
        }

        Note current = list.get(index);

        if ("Trash".equals(getIntent().getStringExtra("caller"))) {
            trashList.remove(index); // Remove from trash can
        } else {
            trashList.add(0, new Note(current.getTitle(), current.getText(), colorPicked,
                    current.getDate()));
            list.remove(index);
        }
        UTIL.saveNotes(list, key);
        UTIL.saveNotes(trashList, "trash");

        Toast.makeText(getApplicationContext(), getString(R.string.delete_toast), Toast.LENGTH_LONG).show();
        // Load previously called activity
        if ("Trash".equals(getIntent().getStringExtra("caller")))
            UTIL.goToActivity(MainActivity.class, "Trash", getApplicationContext());
        else if (isFavorite){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("favorite", true);
            startActivity(intent);
        } else
            UTIL.goToActivity(MainActivity.class, null, getApplicationContext());
    }

    // Restore the note from the trash can to the main notes
    private void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<Note> trash, list;
        trash = UTIL.getNotes("trash");
        list = UTIL.getNotes("notes");

        list.add(0, trash.get(index));
        trash.remove(index);

        UTIL.saveNotes(trash, "trash");
        UTIL.saveNotes(list, "notes");

        Toast.makeText(getApplicationContext(), getString(R.string.restore_toast), Toast.LENGTH_LONG).show();
    }

    // Toolbar Functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;

        // Make sure future calls do not return null pointer
        // Inflate the menu; this adds items to the action bar if it is present.
        if ("Trash".equals(getIntent().getStringExtra("caller")))
            getMenuInflater().inflate(R.menu.trash_note_actions, menu);
        else
            getMenuInflater().inflate(R.menu.edit_actions, menu);

        // Change favorite icon depending on if the current note is favorited or not
        if (isFavorite)
            menu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon_selected);
        else
            menu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("caller", getIntent().getStringExtra("caller"));
                if (prefs.getBoolean("back_dialog_toggle", true))
                    confirmDiscardDialog(MainActivity.class);
                else{
                    finish();
                }
                return true;

            case R.id.action_save:
                saveText();
                return true;

            case R.id.action_star:
                toggleIcon(newFavorite);
                return true;

            case R.id.action_restore:
                restoreNote();
                return true;

            case R.id.action_delete:
                confirmDialog();
                return true;

            case R.id.action_pin:
                showNotification();
                return true;

            case R.id.action_reminder:
                createReminderDialog();
                return true;

            case R.id.action_color:
                colorDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleIcon(boolean fav){
        if (fav){
            mMenu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon);
            newFavorite = false;
        } else {
            mMenu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon_selected);
            newFavorite = true;
        }
    }

    // Dialog Functions
    private void colorDialog(){
        final ColorPicker colorPicker = new ColorPicker(NoteEditActivity.this);
        colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                if (color != 0) {
                    colorPicked = color;

                    CardView cv = findViewById(R.id.card);
                    cv.setCardBackgroundColor(color);
                    //changeViewColor(cv, color);
                    //cv.setRadius(6);
                    //changeViewColor(titleView, color);
                    //changeViewColor(contentView, color);

                    // Change the color of the text depending if the color chosen is dark or not to
                    // make it easier to see for the user
                    if (UTIL.isDarkColor(color)) {
                        contentView.setTextColor(getResources().getColor(R.color.white));
                        titleView.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        contentView.setTextColor(getResources().getColor(R.color.textColor));
                        titleView.setTextColor(getResources().getColor(R.color.textColor));
                    }
                }
            }

            @Override
            public void onCancel(){
                colorPicker.dismissDialog();
            }
        })
            .disableDefaultButtons(false)
            .setColors(getResources().getIntArray(R.array.color_array))
            .setDefaultColorButton(colorPicked)
            .setColumns(4)
            .setRoundColorButton(true)
            .setTitle("Select your color")
            .show();
    }

    // Creates dialog for empty notes
    private void warningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.warning_title));
        builder.setMessage(getString(R.string.warning_confirm));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDiscardDialog(final Class<?> activity){
        View checkBoxView = getLayoutInflater().inflate(R.layout.checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("back_dialog_toggle", false);
                editor.apply();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(checkBoxView);
        builder.setTitle(getString(R.string.discard_title));
        builder.setMessage(getString(R.string.discard_confirm));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { /*
                if ("Trash".equals(getIntent().getStringExtra("caller")))

                    UTIL.goToActivity(activity, "Trash", getApplicationContext());
                else
                    UTIL.goToActivity(activity, null, getApplicationContext()); */
            finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (newFavorite) {
            builder.setTitle(getString(R.string.delete_favorite_title));
            builder.setMessage(getString(R.string.delete_favorite_message));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            builder.setTitle(getString(R.string.delete_title));
            builder.setMessage(getString(R.string.delete_confirm));
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteNote();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Notification Functions
    private Notification buildNotification(String title, String note) {
        // Create an Intent for the activity
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("savedTitle", title);
        intent.putExtra("savedText", note);
        intent.putExtra("caller", getIntent().getStringExtra("caller"));
        intent.putExtra("index", getIntent().getIntExtra("index", 0));
        // Create the TaskStackBuilder and add the intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "NoteChannel")
                .setSmallIcon(R.drawable.event_icon)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(note))
                .setAutoCancel(true)
                .setContentText(note)
                .setContentIntent(resultPendingIntent)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    // Calls notify on the made notification
    private void showNotification(){
        String notifTitle = titleView.getText().toString().trim();
        String notifContent = contentView.getText().toString().trim();
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE); // Unique value for id
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(m, buildNotification(notifTitle, notifContent));
    }

    // Creates the dialog for the scheduled notification.
    private void createReminderDialog() {
        Calendar calendar = Calendar.getInstance();
        // Set as final to be used in inner functions
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create Date Dialog
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog,
        new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yr, int mon, int day) {
                // After selecting date, open up time dialog
                final int y = yr;
                final int m = mon;
                final int d = day;
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hr, int min) {
                        createScheduledNotification(hr, min, y, m, d);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(mContext));
                timePickerDialog.show();
            }
        }, year, month, day);
        datePickerDialog.show();
    }
    // Create a scheduled notification based on user input from previous dialogs
    private void createScheduledNotification(int hour, int min, int year, int month, int day){
        // Create Alarm Manager for Notification
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        SharedPreferences.Editor editor = getSharedPreferences("ID", Context.MODE_PRIVATE).edit();
        editor.putString("title" + id, titleView.getText().toString().trim());
        editor.putString("content" + id, contentView.getText().toString().trim());
        editor.apply();

        Intent notificationIntent = new Intent( this, BroadcastReminder.class);

        notificationIntent.putExtra("gen_id", id);
        notificationIntent.putExtra("index", getIntent().getIntExtra("index", 0));
        notificationIntent.putExtra("caller", getIntent().getStringExtra("caller"));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, 0);

        // Create calendar for scheduled event
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set schedule based on user selection
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(mContext, "Reminder set", Toast.LENGTH_SHORT).show();
    }

    // Shows a dialog when the user presses back while editing a note
    @Override
    public void onBackPressed() {
        if (prefs.getBoolean("back_dialog_toggle", true))
            confirmDiscardDialog(MainActivity.class);
        else
            finish();
    }
}
