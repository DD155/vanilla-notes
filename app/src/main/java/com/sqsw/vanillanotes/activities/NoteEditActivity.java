package com.sqsw.vanillanotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
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
    private SharedPreferences prefs;
    private Context mContext = this;
    private int colorPicked = -1;
    private boolean isOldNote;
    private boolean isTrash = false;
    private boolean isFavorite; // Coming from a previously favorited note
    private boolean newFavorite; // The new favorite value based on user changing it in this activity
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit_layout);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Edit");
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        TextView dateView = findViewById(R.id.date);
        int fontSize = UTIL.getFontSize(getSharedPreferences("NOTES", Context.MODE_PRIVATE).getString("font_size", ""));

        // Check previous activity's extras
        isTrash = "Trash".equals(getIntent().getStringExtra("caller"));
        isOldNote = getIntent().getBooleanExtra("oldNote", false);
        isFavorite = getIntent().getBooleanExtra("favorite", false);

        // Set newFavorite as the value of isFavorite first so the toggle works correctly
        newFavorite = isFavorite;

        // Set attributes of EditTexts
        EditText titleView = findViewById(R.id.titleText);
        EditText textView = findViewById(R.id.editText);

        titleView.setElevation(10);
        textView.setElevation(10);

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize + 3);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        titleView.setPadding(50, 50, 50, 0);
        textView.setPadding(50, 50, 50, 50);

        if (isOldNote) { // Case where user is editing old note
            Note currentNote = getCurrentNote();
            // Set date information; uses substring because original date string includes seconds
            colorPicked = currentNote.getColor();
            String dateString = currentNote.getDate().substring(0, currentNote.getDate().length() - 6)
                    + " " + currentNote.getDate().substring(currentNote.getDate().length() - 2);
            dateView.setText(getString(R.string.date_created, dateString));

            // Set content and title
            titleView.setText(currentNote.getTitle());
            textView.setText(currentNote.getText()); // Set the text on the note page as the old string
            textView.setSelection(textView.getText().length()); // Set cursor to the end
            textView.requestFocus();

            // Set color
            colorPicked = currentNote.getColor();
            Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, colorPicked);
            titleView.setBackground(drawable);
            textView.setBackground(drawable);

            // Set text color depending if color is dark or not
            if (UTIL.isDarkColor(colorPicked)) {
                titleView.setTextColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.white));
            }
        } else {
            String dateString = UTIL.currentDate().substring(0, UTIL.currentDate().length() - 6)
                    + " " + UTIL.currentDate().substring(UTIL.currentDate().length() - 2);
            dateView.setText(getString(R.string.date_created, dateString));

            // Set drawable of new note
            textView.setBackgroundResource(R.drawable.shadow_border);
            titleView.setBackgroundResource(R.drawable.shadow_border);
        }

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        titleView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        refreshDrawables(colorPicked);
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

    // Save the text of the note to the previous activity
    private void saveText(){
        ArrayList<Note> list; // ArrayList for either main notes or trash notes
        String key = "notes";
        int index = getIntent().getIntExtra("index", 0);
        Intent prev = new Intent(getApplicationContext(), MainActivity.class);
        EditText contentView = findViewById(R.id.editText);
        EditText titleView = findViewById(R.id.titleText);

        if (contentView.getText().length() == 0) { // Check that the note is not empty
            warningDialog();
            return;
        }

        // Determine which list to use
        if (isTrash) {
            prev.putExtra("caller", "Trash");
            list = UTIL.getNotes("trash");
            key = "trash";
        } else if (isFavorite){
            prev.putExtra("favorite", true);
            list = UTIL.getNotes("favorites");
            key = "favorites";
        } else list = UTIL.getNotes("notes");


        if (isOldNote) { // Case where the note is old
            Note current = list.get(index);
            // Replace old strings with new strings in note
            if (colorPicked != -1) current.setColor(colorPicked);
            current.setText(contentView.getText().toString().trim());
            current.setTitle(titleView.getText().toString().trim());
            current.setFavorite(newFavorite);

            Log.d("fav_test", "Saving: (New Favorite status) " + newFavorite);

            // If the note is newly favorited, remove it from its current list and add to favorites list
            if (!isFavorite && newFavorite) {
                ArrayList<Note> fav = UTIL.getNotes("favorites");
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
            }
            // Save original note list as usual
            UTIL.saveNotes(list, key);
        } else {
            Note newNote = new Note(titleView.getText().toString().trim(),
                    contentView.getText().toString().trim(),
                    colorPicked,
                    UTIL.currentDate()
            );
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
        startActivity(prev);
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
            trashList.add(0, new Note(current.getTitle(), current.getText(), colorPicked, current.getDate()));
            list.remove(index);
        }
        UTIL.saveNotes(list, key);
        UTIL.saveNotes(trashList, "trash");

        Toast.makeText(getApplicationContext(), getString(R.string.delete_toast), Toast.LENGTH_LONG).show();
        // Load previously called activity
        if ("Trash".equals(getIntent().getStringExtra("caller"))){
            UTIL.goToActivity(MainActivity.class, "Trash", getApplicationContext());
        } else if (isFavorite){
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
        if ("Trash".equals(getIntent().getStringExtra("caller"))) {
            getMenuInflater().inflate(R.menu.trash_note_actions, menu);
        } else {
            getMenuInflater().inflate(R.menu.edit_actions, menu);
        }

        // If the note is not new, check if the current note has been starred and load menu item
        if (isOldNote) {
            // Return boolean value for if the current note is starred or not
            if (isFavorite) {
                menu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon_selected);
            } else {
                menu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon);
                return true;
            }
        } else {
            menu.findItem(R.id.action_star).setIcon(R.drawable.favorite_icon);
        }

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
                    if (isFavorite) intent.putExtra("favorite", true);
                    startActivity(intent);
                }
                return true;

            case R.id.action_save:
                saveText();
                return true;

            case R.id.action_star:
                toggleIcon(isFavorite);
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
                    //ArrayList<Note> notes = util.getNotes("notes");
                    // put code
                    TextView title = findViewById(R.id.titleText);
                    TextView text = findViewById(R.id.editText);
                    Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, color);

                    title.setBackground(drawable);
                    text.setBackground(drawable);

                    if (UTIL.isDarkColor(color)) {
                        ((TextView) findViewById(R.id.editText)).setTextColor(getResources().getColor(R.color.white));
                        ((TextView) findViewById(R.id.titleText)).setTextColor(getResources().getColor(R.color.white));
                    } else {
                        ((TextView) findViewById(R.id.editText)).setTextColor(getResources().getColor(R.color.textColor));
                        ((TextView) findViewById(R.id.titleText)).setTextColor(getResources().getColor(R.color.textColor));
                    }
                }
            }

            @Override
            public void onCancel(){
                // put code
            }
        })
            .disableDefaultButtons(false)
            .setColors(getResources().getIntArray(R.array.color_array))
            .setDefaultColorButton(colorPicked)
            //.setDefaultColor(Color.parseColor("#f84c44"))
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
            public void onClick(DialogInterface dialog, int which) {
                if ("Trash".equals(getIntent().getStringExtra("caller")))
                    UTIL.goToActivity(activity,"Trash", getApplicationContext());
                else
                    UTIL.goToActivity(activity,null, getApplicationContext());
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
        //EditText text = findViewById(R.id.editText);
        //EditText title = findViewById(R.id.titleText);
        //String message = text.getText().toString().trim();
        //String titleMessage = title.getText().toString().trim();

        Log.d("notif_test", title);

        // Create an Intent for the activity
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("savedTitle", title);
        intent.putExtra("savedText", note);
        intent.putExtra("caller", getIntent().getStringExtra("caller"));
        intent.putExtra("index", getIntent().getIntExtra("index", 0));
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NoteChannel")
                .setSmallIcon(R.drawable.ic_baseline_event_note_24)
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
        String title = ((EditText)(findViewById(R.id.titleText))).getText().toString();
        String note = ((EditText)(findViewById(R.id.editText))).getText().toString();
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(m, buildNotification(title, note));
        refreshDrawables(colorPicked);
    }

    // Creates the dialog for the scheduled notification. First opens up date dialog then time dialog.
    private void createReminderDialog() {
        Calendar calendar = Calendar.getInstance();
        // Set as final to be used in inner functions
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String title = ((EditText)(findViewById(R.id.titleText))).getText().toString();
        final String note = ((EditText)(findViewById(R.id.editText))).getText().toString();

        // Create Date Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog,
        new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yr, int mon, int day) {
                // After selecting date, open up time dialog
                final int y = yr;
                final int m = mon;
                final int d = day;
                TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hr, int min) {
                        createScheduledNotification(buildNotification(title, note), hr, min, y, m, d);
                        //Toast.makeText(util, pickedTime, Toast.LENGTH_SHORT).show();
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(mContext));
                timePickerDialog.show();
            }
        }, year, month, day);
        //datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
        datePickerDialog.show();
    }
    // Create a scheduled notification based on user input from previous dialogs
    private void createScheduledNotification(Notification notification, int hour, int min, int year, int month, int day){
        // Create Alarm Manager for Notification
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        Log.d("notif_test2", "Generated ID: " + id);

        SharedPreferences.Editor editor = getSharedPreferences("ID", Context.MODE_PRIVATE).edit();
        editor.putString("title"+id, (((EditText)findViewById(R.id.titleText)).getText().toString().trim()));
        editor.putString("content"+id, (((EditText)findViewById(R.id.editText)).getText().toString().trim()));
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
        refreshDrawables(colorPicked);
        Toast.makeText(mContext, "Reminder set", Toast.LENGTH_SHORT).show();
    }

    // Refresh drawables when creating notifications because they change them for some reason
    private void refreshDrawables(int color){
        findViewById(R.id.titleText).setBackground(UTIL.changeDrawableColor(R.drawable.shadow_border, color));
        findViewById(R.id.editText).setBackground(UTIL.changeDrawableColor(R.drawable.shadow_border, color));
    }

    // Shows a dialog when the user presses back while editing a note
    @Override
    public void onBackPressed() {
        Log.d("back_press_test",
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("back_dialog_toggle", true) + "");
        if (prefs.getBoolean("back_dialog_toggle", true))
            confirmDiscardDialog(MainActivity.class);
        else {
            super.onBackPressed();
        }
    }


}
