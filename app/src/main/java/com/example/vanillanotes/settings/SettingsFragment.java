package com.example.vanillanotes.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.vanillanotes.Note;
import com.example.vanillanotes.R;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.example.vanillanotes.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragmentCompat {

    Activity mActivity;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private final String KEY_FONT = "font_size";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        // Initialize the clear all notes preference
        Preference pref = findPreference("clear");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Clear Data");
                builder.setMessage("Would you like to clear ALL data? Your settings will still be saved.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Note> note = getNotes("notes");
                        ArrayList<Note> trash = getNotes("trash");
                        note.clear();
                        trash.clear();
                        saveNotes(note, "notes");
                        saveNotes(trash, "trash");
                        Toast.makeText(getActivity(), "All data cleared", Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
        // Initialize Font Size Preference
        /*
        ListPreference listPreference = findPreference("font_size");
        String currValue = listPreference.getValue();
        Log.d("curr_val", currValue);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (currValue.equals("small")){
            editor.putString("font_size","small");
        } else if (currValue.equals("medium")){
            editor.putString("font_size","medium");
        } else {
            editor.putString("font_size","large");
        }
        editor.apply();
         */
        ListPreference lp = findPreference(KEY_FONT);
        lp.setSummary(lp.getValue());

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s.equals(KEY_FONT)){
                    ListPreference pref = findPreference(s);
                    pref.setSummary(sharedPreferences.getString(s, ""));
                    SharedPreferences prefs = mActivity.getSharedPreferences("NOTES", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_FONT, ((ListPreference)findPreference(KEY_FONT)).getValue());
                    editor.apply();
                }
            }
        };
    }

    private void saveNotes(ArrayList<Note> list, String key){ // saves the arraylist using gson
        SharedPreferences prefs = this.getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    private ArrayList<Note> getNotes(String key){ //returns the arraylist from sharedprefs
        SharedPreferences prefs = this.getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Prevent retrieving null from getActivity()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity =(Activity) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onPause();
    }
}
