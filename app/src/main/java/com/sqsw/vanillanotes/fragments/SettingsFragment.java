package com.sqsw.vanillanotes.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sqsw.vanillanotes.LoadingDialog;
import com.sqsw.vanillanotes.note.Note;
import com.sqsw.vanillanotes.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.sqsw.vanillanotes.util.PrefsUtil;
import com.sqsw.vanillanotes.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class SettingsFragment extends PreferenceFragmentCompat {

    Activity mActivity;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private final String KEY_FONT = "font_size";
    private final String KEY_BACK_DIALOG = "back_dialog_toggle";
    private final int PERMISSION_CODE = 1;
    private final int EXPORT_CODE = 5;
    private final int IMPORT_CODE = 3;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() == null) {
            Log.e("settings_error", "getActivity() has returned null");
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("More");
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundColor(getResources().getColor(R.color.background));
        getActivity().findViewById(R.id.fam).setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        ListPreference fontPref = findPreference(KEY_FONT);
        Preference clearPref = findPreference("clear");
        Preference exportPref = findPreference("export_backup");
        Preference importPref = findPreference("import_backup");
        SwitchPreferenceCompat backPref = findPreference(KEY_BACK_DIALOG);

        if (fontPref == null || clearPref == null || backPref == null){
            Log.e("settings_error", "Preference initialization has thrown null pointer exception");
            return;
        }

        // Initialize the clear all notes preference
        clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogThemeLight);
                builder.setTitle(getString(R.string.clear_data_dialog_title));
                builder.setMessage(getString(R.string.clear_data_dialog_text));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Note> note = PrefsUtil.getNotes("notes", getActivity());
                        ArrayList<Note> trash = PrefsUtil.getNotes("trash", getActivity());
                        ArrayList<Note> fav = PrefsUtil.getNotes("favorites", getActivity());
                        note.clear();
                        trash.clear();
                        fav.clear();
                        PrefsUtil.saveNotes(note, "notes", getActivity());
                        PrefsUtil.saveNotes(trash, "trash", getActivity());
                        PrefsUtil.saveNotes(fav, "favorites", getActivity());
                        Toast.makeText(getActivity(), getString(R.string.clear_data_toast), Toast.LENGTH_LONG).show();
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

        exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean requireWritePermission = ContextCompat.checkSelfPermission
                        (getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;

                if (requireWritePermission) {
                    requestStoragePermissions(true);
                } else {
                    exportNotes();
                }
                return true;
            }
        });

        importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean requireWritePermission = ContextCompat.checkSelfPermission
                        (getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;

                if (requireWritePermission) {
                    requestStoragePermissions(false);
                } else {
                    chooseFileForImport();
                }
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

    /*
    Data format
    Title: sample title
    Content: sample content
    Favorite: false/true
     */

    private void requestStoragePermissions(final boolean isExport) {
        if (ActivityCompat.shouldShowRequestPermissionRationale
                (getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(getActivity(), R.style.DialogThemeLight)
                    .setTitle(getString(R.string.perm_dialog_title))
                    .setMessage(getString(R.string.perm_dialog_msg))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
                            if (isExport)
                                exportNotes();
                            else
                                chooseFileForImport();
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    private JSONArray getDataAsJSONArray(String key){
        JSONArray noteDataArray = new JSONArray();
        for (Note note : PrefsUtil.getNotes(key, getActivity())){
            try {
                JSONObject noteData = new JSONObject();
                noteData.put("title", note.getTitle());
                noteData.put("content", note.getContent());
                noteData.put("color", note.getColor());
                noteData.put("date", note.getDate());
                noteData.put("favorite", note.getFavorite());
                noteDataArray.put(noteData);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        return noteDataArray;
    }

    private byte[] createExportData(){
        JSONArray notesData = getDataAsJSONArray("notes");
        JSONArray favoritesData = getDataAsJSONArray("favorites");

        JSONObject dataObject = new JSONObject();
        try {
            dataObject.put("Notes", notesData);
            dataObject.put("Favorites", favoritesData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = dataObject.toString();
        Log.d("import_test", data);
        return data.getBytes();
    }

    private void saveFileToStorage() throws IOException {
        String date;
        Calendar calendar = Calendar.getInstance();
        date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1)
                + "-" + calendar.get(Calendar.DAY_OF_MONTH);

        Intent target = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        target.setType("application/*");
        target.putExtra(Intent.EXTRA_TITLE, "vanillanotes-" + date + ".vnotes");
        Intent intent = Intent.createChooser(target, "Save File");
        startActivityForResult(intent, EXPORT_CODE);
    }

    private void writeToFile(Intent intent){
        if (intent != null) {
            Uri uri = intent.getData();
            try {
                OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri);
                outputStream.write(createExportData());
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void exportNotes() {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startDialog();

        try {
            saveFileToStorage();
            loadingDialog.dismissDialog();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void chooseFileForImport(){
        Intent target = new Intent(Intent.ACTION_GET_CONTENT);
        target.setType("application/*");
        target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        target.addCategory(Intent.CATEGORY_OPENABLE);
        Intent intent = Intent.createChooser(target, "Import File");
        startActivityForResult(intent, IMPORT_CODE);
    }

    private JSONObject readFile(Uri uri){
        String content = "";
        try {
            StringBuffer buffer = new StringBuffer();
            ArrayList<Note> notes, favorites;
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            if (inputStream != null){
                while ((content = reader.readLine()) != null){
                    buffer.append(content + "\n");
                }
            }
            content = buffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        
        JSONObject json = null;

        try {
            json = new JSONObject(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void parseFile(JSONObject content){
        if (content == null) return;

        JSONObject notes = content;
        Iterator iterator = notes.keys();
        JSONArray jsonArr = new JSONArray();
        while (iterator.hasNext()){
            String key = iterator.next().toString();
            try {
                jsonArr.put(notes.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            Log.d("file_test", "Notes: " + jsonArr.get(0).toString());
            Log.d("file_test", "Favorites: " + jsonArr.get(1).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("file_test", "JSONARR: " + jsonArr.toString());

    }

    private void importFile(Intent intent){
        if (intent == null) return;

        Uri uri = intent.getData();
        String fileName = Utility.getFileName(uri, getActivity());
        if (Utility.isValidFileType(fileName)){
            JSONObject fileContents = readFile(uri);
            parseFile(fileContents);
        } else {
            showErrorDialog();
        }

    }

    private void showErrorDialog(){
        new AlertDialog.Builder(getActivity(), R.style.DialogThemeLight)
                .setTitle(getString(R.string.import_error_title))
                .setMessage(getString(R.string.import_error_msg))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case EXPORT_CODE:
                    writeToFile(resultData);
                    break;
                case IMPORT_CODE:
                    importFile(resultData);
                    break;
            }
        }
    }

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
