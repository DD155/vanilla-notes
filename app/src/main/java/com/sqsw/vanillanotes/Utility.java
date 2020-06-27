package com.sqsw.vanillanotes;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

// utility class for other activities
public class Utility extends ContextWrapper {

    public final int FONT_SMALL = 12;
    public final int FONT_MEDIUM = 15;
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
        if (pref.equals("Small")){
            return FONT_SMALL;
        } else if (pref.equals("Large")){
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

    // Returns a string of the current time and date in format MM/DD/YY HH:MM
    public String currentDate(){
        Calendar instance = Calendar.getInstance();
        String dayOfWeek = Integer.toString(instance.get(Calendar.DAY_OF_MONTH));
        String hour;
        String minutes;

        if (instance.get(Calendar.MINUTE) / 10 == 0){
            minutes = "0" + (instance.get(Calendar.MINUTE));
        } else
        {
            minutes = Integer.toString(instance.get(Calendar.MINUTE));
        }

        if (instance.get(Calendar.HOUR_OF_DAY) >= 12){
            hour = Integer.toString(instance.get(Calendar.HOUR_OF_DAY) - 12);
            minutes += " PM";
        } else
        {
            hour = Integer.toString(instance.get(Calendar.HOUR_OF_DAY));
            minutes = "AM";
        }

        if (hour.equals("0")) hour = "12";

        String month = Integer.toString(instance.get(Calendar.MONTH) + 1);
        String year = Integer.toString(instance.get(Calendar.YEAR));
        return month + "/" + dayOfWeek + "/" + year + " " + hour + ":" + minutes;
    }
}
