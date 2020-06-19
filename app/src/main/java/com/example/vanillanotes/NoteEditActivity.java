package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.vanillanotes.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class NoteEditActivity extends AppCompatActivity {
    private Utility util = new Utility(this);
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Edit");
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView dateView = findViewById(R.id.date);
        int fontSize = util.getFontSize(getSharedPreferences("NOTES", Context.MODE_PRIVATE).getString("font_size", ""));

        // Set attributes of EditTexts
        String text = getIntent().getStringExtra("savedText");
        String title = getIntent().getStringExtra("savedTitle");
        EditText titleView = findViewById(R.id.titleText);
        EditText textView = findViewById(R.id.editText);

        Log.d("date_time_0", util.currentDate());
        titleView.setPadding(50, 50, 50, 0);
        textView.setPadding(50, 50, 50, 50);

        if (text != null) { // Case where user is editing old note
            Note currentNote = util.getNotes("notes")
                    .get(getIntent().getIntExtra("index", 0));
            dateView.setText(getString(R.string.date_created, currentNote.getDate()));
            //dateView.setText("Date Created: " + currentNote.getDate());
            if (title != null) titleView.setText(title);
            textView.setText(text); // Set the text on the note page as the old string
            textView.setSelection(textView.getText().length()); // Set cursor to the end
            textView.requestFocus(); // Set cursor to this View specifically
        } else {
            dateView.setText(getString(R.string.date_created, util.currentDate()));
        }

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textView.setBackgroundResource(R.drawable.shadow_border);
        titleView.setBackgroundResource(R.drawable.shadow_border);

    }

    // Save the text of the note to the previous activity
    private void saveText(){
        ArrayList<Note> list; // ArrayList for either main notes or trash notes
        String key = "notes";
        Intent prev;
        boolean isTrash = false;
        String text = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        EditText textView = findViewById(R.id.editText);
        EditText titleView = findViewById(R.id.titleText);
        // Make sure future calls do not return null pointer
        if (caller == null) return;

        // Determine if previous activity was trash activity or not
        if (caller.equals("MainActivity")) prev = new Intent(getApplicationContext(), MainActivity.class);
        else {
            prev = new Intent(getApplicationContext(), TrashActivity.class);
            isTrash = true;
        }

        if (textView.getText().length() == 0) { // Check that the note is not empty
            warningDialog();
            return;
        }

        if (text == null) { // Case where the note is new
            prev.putExtra("note", textView.getText().toString());
            prev.putExtra("title", titleView.getText().toString());
            prev.putExtra("date", util.currentDate());
        } else { // Case where the note is being edited
            // Determine which list to use
            if (isTrash) {
                list = util.getNotes("trash");
                key = "trash";
            }
            else list = util.getNotes("notes");

            // Replace old strings with new strings in the ArrayList
            list.get(getIntent().getIntExtra("index", 0)).setText(textView.getText().toString());
            list.get(getIntent().getIntExtra("index", 0)).setTitle(titleView.getText().toString());
            util.saveNotes(list, key);
        }
        startActivity(prev);
    }

    private void deleteNote(){
        String text = getIntent().getStringExtra("savedText");
        String title = getIntent().getStringExtra("savedTitle");
        String caller = getIntent().getStringExtra("caller");
        ArrayList<Note> trashList;

        // Make sure future calls do not return null pointer
        if (caller == null) return;

        if (util.getNotes("trash") != null) // check if trash can list is valid
            trashList = util.getNotes("trash");
        else {
            trashList = new ArrayList<>();
        }
        ArrayList<Note> list  = util.getNotes("notes");


        if (text != null) { // save the note to trash while deleting from main notes
            if (caller.equals("TrashActivity")) {
                trashList.remove(getIntent().getIntExtra("index", 0)); //remove from trash can
            } else {
                trashList.add(new Note(title, text));
                list.remove(getIntent().getIntExtra("index", 0));
            }
            util.saveNotes(list, "notes");
            util.saveNotes(trashList, "trash");
        }
        Toast.makeText(getApplicationContext(), getString(R.string.delete_toast), Toast.LENGTH_LONG).show();

        // Load previously called activity
        if (caller.equals("TrashActivity")){
            util.goToActivity(TrashActivity.class, "NoteEditActivity", getApplicationContext());
        } else {
            util.goToActivity(MainActivity.class, "NoteEditActivity", getApplicationContext());
        }
    }

    // Restore the note from the trash can to the main notes
    private void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<Note> trash, list;
        trash = util.getNotes("trash");
        list = util.getNotes("notes");

        list.add(trash.get(index));
        trash.remove(index);

        util.saveNotes(trash, "trash");
        util.saveNotes(list, "notes");

        Toast.makeText(getApplicationContext(), getString(R.string.restore_toast), Toast.LENGTH_LONG).show();
    }

    // Toolbar Functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Make sure future calls do not return null pointer
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getStringExtra("caller") != null &&
                getIntent().getStringExtra("caller").equals("TrashActivity")) {
            getMenuInflater().inflate(R.menu.trash_note_actions, menu);
        } else
            getMenuInflater().inflate(R.menu.notes_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getIntent().getStringExtra("caller") != null &&
                        getIntent().getStringExtra("caller").equals("MainActivity"))
                    confirmDiscardDialog(MainActivity.class);
                    //intent.setClass(getApplicationContext(), MainActivity.class);
                else
                    confirmDiscardDialog(TrashActivity.class);
                return true;

            case R.id.action_settings:
                // Create dialog if user wants to continue to settings, discarding the current note.
                confirmDiscardDialog(SettingsActivity.class);
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

            case R.id.action_pin:
                showNotification();
                return true;

            case R.id.action_reminder:
                createReminderDialog();
                return true;

            case R.id.action_color:
                /*
                SimpleColorDialog.build()
                        .title("Choose a color")
                        .colorPreset(Color.RED)
                        .allowCustom(true)
                        .show(this);

                 */
                colorDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Dialog Functions

    private void colorDialog(){
        final ColorPicker colorPicker = new ColorPicker(NoteEditActivity.this);
        colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                // put code
                TextView title = findViewById(R.id.titleText);
                //getResources().getDrawable
                title.setBackgroundColor(color);
            }

            @Override
            public void onCancel(){
                // put code
            }
        })
            .disableDefaultButtons(false)
            .setColors(getResources().getIntArray(R.array.color_array))
            .setDefaultColorButton(getResources().getColor(R.color.white))
            //.setDefaultColor(Color.parseColor("#f84c44"))
            .setColumns(5)
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

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDiscardDialog(final Class<?> activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.discard_title));
        builder.setMessage(getString(R.string.discard_confirm));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                util.goToActivity(activity,"NoteEditActivity", getApplicationContext());
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

    private void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Notification Functions

    private Notification buildNotification() {
        EditText text = findViewById(R.id.editText);
        EditText title = findViewById(R.id.titleText);
        String message = text.getText().toString();
        String titleMessage = title.getText().toString();

        // Create an Intent for the activity
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("savedTitle", titleMessage);
        intent.putExtra("savedText", message);
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
                .setContentTitle(title.getText())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentText(message)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    // Calls notify on the made notification
    private void showNotification(){
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100, buildNotification());
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
                        createScheduledNotification(buildNotification(), hr, min, y, m, d);
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
        Intent notificationIntent = new Intent( this, BroadcastReminder. class );
        notificationIntent.putExtra("notification", notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);

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
    }

    // Shows a dialog when the user presses back while editing a note
    @Override
    public void onBackPressed() {
        // Make sure future calls do not return null pointer
        if (getIntent().getStringExtra("caller") != null &&
                getIntent().getStringExtra("caller").equals("MainActivity"))
            confirmDiscardDialog(MainActivity.class);
        else
            confirmDiscardDialog(TrashActivity.class);
    }
}
