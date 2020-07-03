package com.sqsw.vanillanotes.activities;

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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
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

import com.sqsw.vanillanotes.classes.BroadcastReminder;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NoteEditActivity extends AppCompatActivity {
    private final Utility UTIL = new Utility(this);
    private Context mContext = this;
    private int colorPicked = -1;
    private boolean isTrash = false;
    private boolean isNew = false;
    private boolean isStarred;
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

        TextView dateView = findViewById(R.id.date);
        int fontSize = UTIL.getFontSize(getSharedPreferences("NOTES", Context.MODE_PRIVATE).getString("font_size", ""));

        // Check previous activity's caller
        isTrash = false;
        if ("Trash".equals(getIntent().getStringExtra("caller"))) isTrash = true;

        // Set attributes of EditTexts
        String text = getIntent().getStringExtra("savedText");
        String title = getIntent().getStringExtra("savedTitle");
        EditText titleView = findViewById(R.id.titleText);
        EditText textView = findViewById(R.id.editText);

        titleView.setPadding(50, 50, 50, 0);
        textView.setPadding(50, 50, 50, 50);


        if (text != null) { // Case where user is editing old note
            Note currentNote = getCurrentNote();

            colorPicked = currentNote.getColor();
            dateView.setText(getString(R.string.date_created, currentNote.getDate()));

            //dateView.setText("Date Created: " + currentNote.getDate());
            if (title != null) titleView.setText(title);
            textView.setText(text); // Set the text on the note page as the old string
            textView.setSelection(textView.getText().length()); // Set cursor to the end
            textView.requestFocus();
        } else
            isNew = true;
            dateView.setText(getString(R.string.date_created, UTIL.currentDate()));

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        titleView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // Refresh drawables when focused on editing title
        titleView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                refreshDrawables(colorPicked);
            }
        });

        // Change color of the background depending on what the user chose
        if (getIntent().getIntExtra("color", 0) != -1 && getIntent().getIntExtra("color", 0) != 0){
            colorPicked = getIntent().getIntExtra("color", 0);
            Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, getIntent().getIntExtra("color", 0));
            titleView.setBackground(drawable);
            textView.setBackground(drawable);

            if (UTIL.isDarkColor(colorPicked)) {
                titleView.setTextColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.white));
            }

        } else {
            textView.setBackgroundResource(R.drawable.shadow_border);
            titleView.setBackgroundResource(R.drawable.shadow_border);
        }

        Log.d("trash_debug", "reached end of oncreate");
    }

    // Retrive ArrayList depending on if user entered from activity trash or home
    private Note getCurrentNote(){
        Note currentNote;
        if (!isTrash)
            currentNote = UTIL.getNotes("notes")
                    .get(getIntent().getIntExtra("index", 0));
        else
            currentNote = UTIL.getNotes("trash")
                    .get(getIntent().getIntExtra("index", 0));

        return currentNote;
    }

    // Save the text of the note to the previous activity
    private void saveText(){
        ArrayList<Note> list; // ArrayList for either main notes or trash notes
        String key = "notes";
        Intent prev = new Intent(getApplicationContext(), MainActivity.class);
        String text = getIntent().getStringExtra("savedText");
        String caller = getIntent().getStringExtra("caller");
        EditText textView = findViewById(R.id.editText);
        EditText titleView = findViewById(R.id.titleText);
        // Make sure future calls do not return null pointer
        if (caller == null) return;

        // Determine if previous activity was trash activity or not
        if ("Trash".equals(caller)) {
            prev.putExtra("caller", "Trash");
            isTrash = true;
        }

        if (textView.getText().length() == 0) { // Check that the note is not empty
            warningDialog();
            return;
        }

        if (text == null) { // Case where the note is new
            prev.putExtra("note", textView.getText().toString().trim());
            prev.putExtra("title", titleView.getText().toString().trim());
            prev.putExtra("date", UTIL.currentDate());
            prev.putExtra("star", isStarred);
            prev.putExtra("color", colorPicked);
        } else { // Case where the note is being edited
            // Determine which list to use
            if (isTrash) {
                list = UTIL.getNotes("trash");
                key = "trash";
            }
            else list = UTIL.getNotes("notes");

            Note current = list.get(getIntent().getIntExtra("index", 0));
            // Replace old strings with new strings in the ArrayList
            if (colorPicked != -1)
                current.setColor(colorPicked);
            current.setText(textView.getText().toString().trim());
            current.setTitle(titleView.getText().toString().trim());
            current.setStarred(isStarred);
            UTIL.saveNotes(list, key);
        }
        startActivity(prev);
    }

    private void deleteNote(){
        String text = getIntent().getStringExtra("savedText");
        String title = getIntent().getStringExtra("savedTitle");
        String caller = getIntent().getStringExtra("caller");
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<Note> trashList;

        // Make sure future calls do not return null pointer
        if (caller == null) return;

        if (UTIL.getNotes("trash") != null) // check if trash can list is valid
            trashList = UTIL.getNotes("trash");
        else {
            trashList = new ArrayList<>();
        }
        ArrayList<Note> list  = UTIL.getNotes("notes");

        String date = list.get(index).getDate();


        if (text != null) { // save the note to trash while deleting from main notes
            if (caller.equals("Trash")) {
                trashList.remove(getIntent().getIntExtra("index", 0)); //remove from trash can
            } else {
                trashList.add(new Note(title, text, colorPicked, list.get(index).getDate()));
                list.remove(index);
            }
            UTIL.saveNotes(list, "notes");

            UTIL.saveNotes(trashList, "trash");
        }
        Toast.makeText(getApplicationContext(), getString(R.string.delete_toast), Toast.LENGTH_LONG).show();

        // Load previously called activity
        if (caller.equals("Trash")){
            UTIL.goToActivity(MainActivity.class, "Trash", getApplicationContext());
        } else {
            UTIL.goToActivity(MainActivity.class, null, getApplicationContext());
        }
    }

    // Restore the note from the trash can to the main notes
    private void restoreNote(){
        int index = getIntent().getIntExtra("index", 0);
        ArrayList<Note> trash, list;
        trash = UTIL.getNotes("trash");
        list = UTIL.getNotes("notes");

        Log.d("date_test", "Date: " + trash.get(index).getDate());

        list.add(trash.get(index));
        trash.remove(index);

        Log.d("date_test", list.get(list.size() - 1).getDate());

        UTIL.saveNotes(trash, "trash");
        UTIL.saveNotes(list, "notes");

        Toast.makeText(getApplicationContext(), getString(R.string.restore_toast), Toast.LENGTH_LONG).show();
    }

    // Toolbar Functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        ArrayList<Note> list;
        if (isTrash) list = UTIL.getNotes("trash");
        else list = UTIL.getNotes("notes");

        // Make sure future calls do not return null pointer
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getStringExtra("caller") != null) {
            if ("Trash".equals(getIntent().getStringExtra("caller"))) {
                getMenuInflater().inflate(R.menu.trash_note_actions, menu);
            } else
                getMenuInflater().inflate(R.menu.edit_actions, menu);
        } else {
            Log.e("NoteActivity", "Caller is null");
        }

        // If the note is not new, check if the current note has been starred and load menu item
        if (!isNew) {
            // Return boolean value for if the current note is starred or not
            if (list.get(getIntent().getIntExtra("index", 0)).getStarred()) {
                menu.findItem(R.id.action_star).setIcon(R.drawable.star_selected_icon);
                isStarred = true;
                //menu.findItem(R.id.action_starred).setVisible(true);
            } else {
                menu.findItem(R.id.action_star).setIcon(R.drawable.star_icon);
                isStarred = false;
                return true;
            }
        } else {
            isStarred = false;
            menu.findItem(R.id.action_star).setIcon(R.drawable.star_icon);
        }

        Log.d("trash_debug", "oncreateoptions end");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                confirmDiscardDialog(MainActivity.class);
                return true;

            case R.id.action_save:
                saveText();
                return true;

            case R.id.action_star:
                toggleIcon(isStarred);
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

    private void toggleIcon(boolean star){
        if (star){
            mMenu.findItem(R.id.action_star).setIcon(R.drawable.star_icon);
            isStarred = false;
        } else {
            mMenu.findItem(R.id.action_star).setIcon(R.drawable.star_selected_icon);
            isStarred = true;
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
                Log.d("caller_test", "Edit Actiivty: " + getIntent().getStringExtra("caller"));
                if ("Trash".equals(getIntent().getStringExtra("caller")))
                    UTIL.goToActivity(activity,"Trash", getApplicationContext());
                else
                    UTIL.goToActivity(activity,null, getApplicationContext());
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

    // Shows a dialog when the user presses back while editing a note
    @Override
    public void onBackPressed() {
        // Make sure future calls do not return null pointer
        if (getIntent().getStringExtra("caller") != null) {
            /*
            if ("MainActivity".equals(getIntent().getStringExtra("caller")))
                confirmDiscardDialog(MainActivity.class);
            else
                confirmDiscardDialog(TrashActivity.class);

             */
            confirmDiscardDialog(MainActivity.class);
        } else {
            Log.e("NoteActivity", "Caller is null");
        }
    }

    // Refresh drawables when creating notifications because they change them for some reason
    private void refreshDrawables(int color){
        findViewById(R.id.titleText).setBackground(UTIL.changeDrawableColor(R.drawable.shadow_border, color));
        findViewById(R.id.editText).setBackground(UTIL.changeDrawableColor(R.drawable.shadow_border, color));
    }
}
