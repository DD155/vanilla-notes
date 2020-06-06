package com.example.vanillanotes.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vanillanotes.Note;
import com.example.vanillanotes.R;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.example.vanillanotes.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragmentCompat {

    Activity mActivity;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
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
    }

    public void saveNotes(ArrayList<Note> list, String key){ // saves the arraylist using gson
        SharedPreferences prefs = this.getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<Note> getNotes(String key){ //returns the arraylist from sharedprefs
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
}
