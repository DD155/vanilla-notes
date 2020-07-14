package com.sqsw.vanillanotes.classes;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sqsw.vanillanotes.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

// utility class for other com.sqsw.vanillanotes.activities
public class Utility extends ContextWrapper {

    public final int FONT_SMALL = 11;
    public final int FONT_MEDIUM = 14;
    public final int FONT_LARGE = 17;

    public Utility(Context base) {
        super(base);
    }

    // Creates intent with information of what previous class called the new activity
    public void goToActivity(Class<?> act, String s, Context context){
        Intent i = new Intent(context, act);
        i.putExtra("caller", s);
        startActivity(i);
    }

    // Saves the ArrayList using Gson
    public void saveNotes(ArrayList<Note> list, String key){
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    // Returns the ArrayList from sharedprefs
    public ArrayList<Note> getNotes(String key){
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        if (gson.fromJson(json, type) == null) return new ArrayList<>();
        return gson.fromJson(json, type);
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
    public int getFontSize(String pref){
        if ("Small".equals(pref)){
            return FONT_SMALL;
        } else if ("Large".equals(pref)){
            return FONT_LARGE;
        } else return FONT_MEDIUM;
    }

    // Returns a drawable that has a color filter applied
    public Drawable changeDrawableColor(int drawableID, int color){
        Drawable d = getResources().getDrawable(drawableID);
        d.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return d;
    }

    // Replaces getDrawable method
    public Drawable returnDrawable(int id){
        Drawable d;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            d = getResources().getDrawable(id, null);
        } else
        {
            d = getResources().getDrawable(id);
        }
        return d;
    }

    // Return a hex code string from R.color int
    public String hexFromColorInt(int color){
        return String.format("#%06X", (0xFFFFFF & color)).substring(1);
    }

    public String getDarkerColor(int color){
        // Logic for making pressed down color a darker shade
        String[] rgbStr = {(hexFromColorInt(color)).substring(0, 2),
                (hexFromColorInt(color)).substring(2, 4),
                (hexFromColorInt(color)).substring(4)
        };
        double[] rgb = { // Divide RGB value to make the result darker
                Math.round(Integer.valueOf(rgbStr[0], 16) * 0.85),
                Math.round(Integer.valueOf(rgbStr[1], 16) * 0.85),
                Math.round(Integer.valueOf(rgbStr[2], 16) * 0.85)
        };
        // Format string in #RRGGBB style
        return String.format("#%02X%02X%02X", (int)rgb[0], (int)rgb[1], (int)rgb[2]);
    }

    // Returns boolean if the given color is dark or not. Used to change text color for readability
    public boolean isDarkColor(int color){
        return color == getResources().getColor(R.color.red) || color == getResources().getColor(R.color.blue) ||
                color == getResources().getColor(R.color.purple) || color == getResources().getColor(R.color.green);
    }

    public int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }

    public String addEllipsis(String str){
        String s;
        if (str.length() >= 83){
            s = str.substring(0, 75) + "…";
        } else {
            s = str + "…";
        }
        return s;
    }

    // Returns a string of the current time and date in format MM/DD/YY HH:MM:SS
    public String currentDate(){
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

        if (hour.length() == 1) return month + "/" + dayOfWeek + "/" + year + " " + "0" + hour + ":" + minutes;
        return month + "/" + dayOfWeek + "/" + year + " " + hour + ":" + minutes;
    }

    public void sortNotes(int type, ArrayList<Note> notes, NotesAdapter adapter, String key){
        switch (type){
            case 0:
                // Type 0 = Sort by Title (Ascending)
                Log.d("selected_index", "case 0");
                Collections.sort(notes, new NoteComparator());
                saveNotes(notes, key);
                adapter.notifyDataSetChanged();
                break;
            case 1:
                // Type 1 = Sort by Title (Descending)
                Log.d("selected_index", "case 1");
                Collections.sort(notes, new NoteComparator());
                Collections.reverse(notes);
                saveNotes(notes, key);
                adapter.notifyDataSetChanged();
                break;
            case 2:
                // Type 2 = Sort by Date Created (Ascending)
                Collections.sort(notes, new DateComparator());
                saveNotes(notes, key);
                adapter.notifyDataSetChanged();
                break;
            case 3:
                // Type 3 = Sort by Date Created (Descending)
                Collections.sort(notes, new DateComparator());
                Collections.reverse(notes);
                saveNotes(notes, key);
                adapter.notifyDataSetChanged();
                break;
            case 4:
                break;
        }
    }
}
