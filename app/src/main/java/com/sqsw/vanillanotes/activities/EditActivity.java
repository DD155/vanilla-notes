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
import android.graphics.Color;
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

import com.sqsw.vanillanotes.BroadcastReminder;
import com.sqsw.vanillanotes.note.Note;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.util.PrefsUtil;
import com.sqsw.vanillanotes.util.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EditActivity extends AppCompatActivity {
    private boolean isTrash;
    private boolean isFavorite;
    private boolean isOldNote;
    private int colorPicked;
    private int index;
    private boolean newFavorite;

    private SharedPreferences prefs;
    private Context mContext;
    private Menu mMenu;
    private ArrayList<Note> notesList;
    private EditText contentView;
    private EditText titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit_layout);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d("edit_test", "1");

        // Initialize fields
        isTrash = getIntent().getBooleanExtra("trash", false);
        isFavorite = getIntent().getBooleanExtra("favorite", false);
        isOldNote = getIntent().getBooleanExtra("oldNote", false);
        index = getIntent().getIntExtra("index", 0);
        colorPicked = Color.WHITE;
        mContext = this;

        TextView dateView = findViewById(R.id.date);
        titleView = findViewById(R.id.titleText);
        contentView = findViewById(R.id.editText);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int fontSize = Utility.getFontSize(prefs.getString("font_size", ""));
        notesList = getCurrentNoteList();

        // Set newFavorite as the value of isFavorite first so the toggle works correctly
        newFavorite = isFavorite;

        // Set attributes of EditTexts
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSize + 5));
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        contentView.setScrollY(0);

        if (isOldNote) { // Case where user is editing old note
            Note currentNote = getCurrentNoteList().get(index);
            // Set date information; uses substring because original date string includes seconds
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
            if (Utility.isDarkColor(colorPicked, this)) {
                titleView.setTextColor(getResources().getColor(R.color.white));
                contentView.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    // Retrive ArrayList depending on if user entered from activity trash or home
    private ArrayList<Note> getCurrentNoteList(){
        if (isTrash)
            return PrefsUtil.getNotes("trash", this);
        else if (isFavorite)
            return PrefsUtil.getNotes("favorites", this);
        else {
            return PrefsUtil.getNotes("notes", this);
        }
    }

    // Helper function for getting the correct notes depending on the user's previous fragment
    private String returnKeyFromList(){
        if (isTrash) {
            return "trash";
        } else if (isFavorite){
            return "favorites";
        } else return "notes";
    }

    private void putExtraValue(Intent intent){
        if (isTrash) {
            intent.putExtra("trash", true);
        } else if (isFavorite) {
            intent.putExtra("favorite", true);
        }
    }

    private void saveOldNote(Note note){
        notesList.remove(index);

        // If the note is NOT originally a favorited note and is now being favorited by the user
        if (!isFavorite && newFavorite) {
            ArrayList<Note> fav = PrefsUtil.getNotes("favorites", this);
            fav.add(0, note);
            PrefsUtil.saveNotes(fav, "favorites", this);
        } else if (isFavorite && !newFavorite){
            // If the note is being unfavorited
            ArrayList<Note> notes = PrefsUtil.getNotes("notes", this);
            notes.add(0, note);
            PrefsUtil.saveNotes(notes, "notes", this);
        } else {
            notesList.add(0, note);
        }
    }

    // Save the attributes (title, text, color, favorite) of the note to the previous activity
    private void saveText(){
        Intent intent = new Intent(this, MainActivity.class);
        String contentText = contentView.getText().toString().trim();
        String titleText = titleView.getText().toString().trim();

        // Check that the note is not empty
        if (contentView.length() == 0) {
            warningDialog();
            return;
        }

        if (isOldNote) {
            // Set new attributes to the note
            Note note = getCurrentNoteList().get(index);
            note.setColor(colorPicked);
            note.setText(contentText);
            note.setTitle(titleText);
            note.setFavorite(newFavorite);

            saveOldNote(note);
        } else {
            Note newNote = Note.createWithFavorite(titleText, contentText, newFavorite);
            if (newFavorite) { // If new note is favorited, add it to the favorites list
                ArrayList<Note> fav = PrefsUtil.getNotes("favorites", this);
                fav.add(0, newNote);
                PrefsUtil.saveNotes(fav, "favorites", this);
            } else {
                notesList.add(0, newNote);
            }
        }
        PrefsUtil.saveNotes(notesList, returnKeyFromList(), this);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        putExtraValue(intent);
        startActivity(intent);
        EditActivity.this.finish();
    }

    // Helper function of deleteNote(): removes the note from the ArrayList and adds to trash,
    // permanently removes if the current list is the trash
    private void removeNoteFromList(){
        ArrayList<Note> trashList = PrefsUtil.getNotes("trash", this);

        if (isTrash) { // Remove from trash can
            trashList.remove(index);
        } else {
            trashList.add(0, notesList.get(index));
            notesList.remove(index);
        }
        PrefsUtil.saveNotes(notesList, returnKeyFromList(), this);
        PrefsUtil.saveNotes(trashList, "trash", this);
    }

    // Menu action: deletes the current note from its list
    private void deleteNote(){
        // If it is a new note, just go back to the main activity
        if (!isOldNote){
            Utility.goToActivity(MainActivity.class, this);
            return;
        }
        removeNoteFromList();

        Toast.makeText(this, getString(R.string.delete_toast), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        putExtraValue(intent);
        startActivity(intent);
        EditActivity.this.finish();
    }

    // Menu action: restores the note from the trash can to the main notes
    private void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<Note> trash, list;
        trash = PrefsUtil.getNotes("trash", this);
        list = PrefsUtil.getNotes("notes", this);

        list.add(0, trash.get(index));
        trash.remove(index);

        PrefsUtil.saveNotes(trash, "trash", this);
        PrefsUtil.saveNotes(list, "notes", this);

        Utility.goToActivity(MainActivity.class, this);
        Toast.makeText(getApplicationContext(), getString(R.string.restore_toast), Toast.LENGTH_LONG).show();
    }

    private void toggleIcon(){
        if (newFavorite){
            mMenu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon);
            newFavorite = false;
        } else {
            mMenu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon_selected);
            newFavorite = true;
        }
    }

    // Dialog Functions
    private void colorDialog(){
        final ColorPicker colorPicker = new ColorPicker(EditActivity.this);
        colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                if (color != 0) {
                    colorPicked = color;

                    CardView cv = findViewById(R.id.card);
                    cv.setCardBackgroundColor(color);

                    // Change the color of the text depending if the color chosen is dark or not to
                    // make it easier to see for the user
                    if (Utility.isDarkColor(color, mContext)) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogThemeLight);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogThemeLight);
        builder.setView(checkBoxView);
        builder.setTitle(getString(R.string.discard_title));
        builder.setMessage(getString(R.string.discard_confirm));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogThemeLight);
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
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("savedTitle", title);
        intent.putExtra("savedText", note);
        intent.putExtra("favorite", isFavorite);
        intent.putExtra("trash", isTrash);
        intent.putExtra("index", index);
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
        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE); // Unique value for id
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(id, buildNotification(notifTitle, notifContent));
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
                new DatePickerDialog(mContext, R.style.DialogThemeLight,
        new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yr, int mon, int day) {
                // After selecting date, open up time dialog
                final int y = yr;
                final int m = mon;
                final int d = day;
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(mContext, R.style.DialogThemeLight, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hr, int min) {
                        Calendar newTime = Calendar.getInstance();
                        newTime.set(y, m, d, hr, min);
                        createScheduledNotification(newTime);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(mContext));
                timePickerDialog.show();
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private Calendar createScheduledCalendar(Calendar scheduledTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set schedule based on user selection
        calendar.set(Calendar.DAY_OF_MONTH, scheduledTime.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH, scheduledTime.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, scheduledTime.get(Calendar.YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, scheduledTime.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, scheduledTime.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);

        return calendar;
    }

    // Create a scheduled notification based on user input from previous dialogs
    private void createScheduledNotification(Calendar scheduledTime){
        // Create Alarm Manager for Notification
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        SharedPreferences.Editor editor = getSharedPreferences("ID", Context.MODE_PRIVATE).edit();
        editor.putString("title" + id, titleView.getText().toString().trim());
        editor.putString("content" + id, contentView.getText().toString().trim());
        editor.apply();

        Intent notificationIntent = new Intent( this, BroadcastReminder.class);
        notificationIntent.putExtra("generatedID", id);
        notificationIntent.putExtra("index", index);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, 0);

        Calendar setScheduledTime = createScheduledCalendar(scheduledTime);
        alarmManager.set(AlarmManager.RTC_WAKEUP, setScheduledTime.getTimeInMillis(), pendingIntent);
        Toast.makeText(mContext, "Reminder set", Toast.LENGTH_SHORT).show();
    }

    // Toolbar Functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        if (isTrash)
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

    private void homeAction(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("caller", getIntent().getStringExtra("caller"));
        if (prefs.getBoolean("back_dialog_toggle", true))
            confirmDiscardDialog(MainActivity.class);
        else{
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                homeAction();
                return true;

            case R.id.action_save:
                saveText();
                return true;

            case R.id.action_star:
                toggleIcon();
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

    // Shows a dialog when the user presses back while editing a note
    @Override
    public void onBackPressed() {
        if (prefs.getBoolean("back_dialog_toggle", true))
            confirmDiscardDialog(MainActivity.class);
        else
            finish();
    }
}
