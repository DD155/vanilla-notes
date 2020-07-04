package com.sqsw.vanillanotes.nav_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragmentCompat {

    Activity mActivity;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private final String KEY_FONT = "font_size";
    private final String KEY_BACK_DIALOG = "back_dialog_toggle";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() == null) {
            Log.e("settings_error", "getActivity() has returned null");
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("More");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        ListPreference fontPref = findPreference(KEY_FONT);
        Preference clearPref = findPreference("clear");
        SwitchPreferenceCompat backPref = findPreference(KEY_BACK_DIALOG);

        if (fontPref == null || clearPref == null || backPref == null){
            Log.e("settings_error", "Preference initialization has thrown null pointer exception");
            return;
        }
        // Initialize the clear all notes preference

        clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.clear_data_dialog_title));
                builder.setMessage(getString(R.string.clear_data_dialog_text));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Note> note = getNotes("notes");
                        ArrayList<Note> trash = getNotes("trash");
                        note.clear();
                        trash.clear();
                        saveNotes(note, "notes");
                        saveNotes(trash, "trash");
                        Toast.makeText(getActivity(), getString(R.string.clear_data_summary), Toast.LENGTH_LONG).show();
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

        // Initalize Font Size preference
        fontPref.setSummary(fontPref.getValue());

        // Initalize Back Dialog preference
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("back_dialog_toggle", false)){
            backPref.setSummary("Enabled");
        } else {
            backPref.setSummary("Disabled");
        }

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s.equals(KEY_FONT)){
                    ListPreference pref = findPreference(s);
                    pref.setSummary(sharedPreferences.getString(s, ""));
                }

                if (s.equals(KEY_BACK_DIALOG)){
                    boolean toggle = sharedPreferences.getBoolean("back_dialog_toggle", false);
                    SwitchPreferenceCompat pref = findPreference(s);
                    if (toggle){
                        pref.setSummary("Enabled");
                    } else {
                        pref.setSummary("Disabled");
                    }
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
    public void onAttach(@NonNull Context context) {
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
