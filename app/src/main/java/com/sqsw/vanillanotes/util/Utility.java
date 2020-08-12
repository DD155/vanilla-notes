package com.sqsw.vanillanotes.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.EditActivity;
import com.sqsw.vanillanotes.note.Note;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

// utility class for other com.sqsw.vanillanotes.activities
public class Utility extends ContextWrapper {

    public static final int FONT_SMALL = 11;
    public static final int FONT_MEDIUM = 14;
    public static final int FONT_LARGE = 17;

    public Utility(Context base) {
        super(base);
    }

    // Creates intent with information of what previous class called the new activity
    public static void goToActivity(Class<?> activity, Context context){
        Intent i = new Intent(context, activity);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public static void goToActivityFromTrash(Context context){
        Intent i = new Intent(context, EditActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("trash", true);
        context.startActivity(i);
    }

    public static void goToActivityFromFav(Context context){
        Intent i = new Intent(context, EditActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("favorite", true);
        context.startActivity(i);
    }

    // Returns the value of the system navigation bar height
    public int getNavigationBarSize(Context context){
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    // Returns the size of the font depending on what user setting is
    public static int getFontSize(String pref){
        if ("Small".equals(pref)){
            return FONT_SMALL;
        } else if ("Large".equals(pref)){
            return FONT_LARGE;
        } else return FONT_MEDIUM;
    }

    // Return a hex code string from R.color int
    public String hexFromColorInt(int color){
        return String.format("#%06X", (0xFFFFFF & color)).substring(1);
    }

    // Returns boolean if the given color is dark or not. Used to change text color for readability
    public static boolean isDarkColor(int color, Context context){
        return color == context.getResources().getColor(R.color.red) ||
                color == context.getResources().getColor(R.color.blue) ||
                color == context.getResources().getColor(R.color.purple) ||
                color == context.getResources().getColor(R.color.green);
    }

    public int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }

    public String addEllipsis(String str, int maxChars){
        String s;
        if (str.length() >= maxChars){
            s = str.substring(0, maxChars - 3) + "…";
        } else {
            s = str;
        }
        return s;
    }

    // Returns a string of the current time and date in format MM/DD/YY HH:MM:SS
    public static String currentDate(){
        Calendar instance = Calendar.getInstance();
        String dayOfWeek;
        String hour;
        String minutes;
        String seconds;
        String month;

        if (Integer.toString(instance.get(Calendar.SECOND)).length() == 1)
            seconds = "0" + Integer.toString(instance.get(Calendar.SECOND)).length();
        else
            seconds = Integer.toString(instance.get(Calendar.SECOND));

        if (instance.get(Calendar.MINUTE) / 10 == 0){
            minutes = "0" + (instance.get(Calendar.MINUTE));
        } else if (instance.get(Calendar.MINUTE) == 0){
            minutes = "00";
        } else
        {
            minutes = Integer.toString(instance.get(Calendar.MINUTE));
        }

        if (instance.get(Calendar.HOUR_OF_DAY) >= 12){

            hour = Integer.toString(instance.get(Calendar.HOUR_OF_DAY) - 12);
            minutes += ":" + seconds + " PM";
        } else
        {
            hour = Integer.toString(instance.get(Calendar.HOUR_OF_DAY));
            minutes = ":" + seconds + "AM";
        }

        if (hour.equals("0")) hour = "12";

        if (Integer.toString(instance.get(Calendar.MONTH) + 1).length() == 1)
            month = "0" + (instance.get(Calendar.MONTH) + 1);
        else
            month = Integer.toString(instance.get(Calendar.MONTH) + 1);

        if (Integer.toString(instance.get(Calendar.DAY_OF_MONTH)).length() == 1)
            dayOfWeek = "0" + instance.get(Calendar.DAY_OF_MONTH);
        else
            dayOfWeek = Integer.toString(instance.get(Calendar.DAY_OF_MONTH));

        String year = Integer.toString(instance.get(Calendar.YEAR));

        if (hour.length() == 1)
            return month + "/" + dayOfWeek + "/" + year + " " + "0" + hour + ":" + minutes;
        return month + "/" + dayOfWeek + "/" + year + " " + hour + ":" + minutes;
    }

    public static String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static boolean isValidFileType(String fileName){
    //    if (")".equals(fileName.charAt(fileName.length() - 1))) {
    //        return "vnotes".equals(fileName.substring(fileName.length() - 6));
    //    } else {
            return "vnotes".equals(fileName.substring(fileName.length() - 6));
    //    }
    }

    public void sortNotes(int type, ArrayList<Note> notes, String key){
        switch (type){
            case 0:
                // Type 0 = Sort by Title (Ascending)
                Log.d("selected_index", "case 0");
                Collections.sort(notes, new NoteComparator());
                PrefsUtil.saveNotes(notes, key, this);
                break;
            case 1:
                // Type 1 = Sort by Title (Descending)
                Log.d("selected_index", "case 1");
                Collections.sort(notes, new NoteComparator());
                Collections.reverse(notes);
                PrefsUtil.saveNotes(notes, key, this);
                break;
            case 2:
                // Type 2 = Sort by Date Created (Ascending)
                Collections.sort(notes, new DateComparator());
                PrefsUtil.saveNotes(notes, key, this);
                break;
            case 3:
                // Type 3 = Sort by Date Created (Descending)
                Collections.sort(notes, new DateComparator());
                Collections.reverse(notes);
                PrefsUtil.saveNotes(notes, key,this);
                break;
            case 4:
                break;
        }
    }
}
