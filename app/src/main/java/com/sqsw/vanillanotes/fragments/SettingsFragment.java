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

import com.github.clans.fab.FloatingActionMenu;
import com.sqsw.vanillanotes.LoadingDialog;
import com.sqsw.vanillanotes.model.Note;
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
import com.sqsw.vanillanotes.util.GeneralUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingsFragment extends PreferenceFragmentCompat {

    Activity mActivity;
    Context mContext;
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

        mContext = getActivity();
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("More");
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundColor(getResources().getColor(R.color.background));
        FloatingActionMenu fam = getActivity().findViewById(R.id.fam);
        fam.close(true);
        fam.setVisibility(View.GONE);
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
            Log.e("settings_error", "Preferences have thrown null pointer exception");
            return;
        }

        // Initialize the clear all notes preference
        clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogThemeLight);
                builder.setTitle(getString(R.string.clear_data_dialog_title));
                builder.setMessage(getString(R.string.clear_data_dialog_text));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Note> note = PrefsUtil.getNotes("notes", mContext);
                        ArrayList<Note> trash = PrefsUtil.getNotes("trash", mContext);
                        ArrayList<Note> fav = PrefsUtil.getNotes("favorites", mContext);
                        note.clear();
                        trash.clear();
                        fav.clear();
                        PrefsUtil.saveNotes(note, "notes", mContext);
                        PrefsUtil.saveNotes(trash, "trash", mContext);
                        PrefsUtil.saveNotes(fav, "favorites", mContext);
                        Toast.makeText(mContext, getString(R.string.clear_data_toast), Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
                        (requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                        (requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        if (PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("back_dialog_toggle", false)){
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
                (requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(mContext, R.style.DialogThemeLight)
                    .setTitle(getString(R.string.perm_dialog_title))
                    .setMessage(getString(R.string.perm_dialog_msg))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{
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
            ActivityCompat.requestPermissions(mActivity,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    private JSONArray getDataAsJSONArray(){
        JSONArray noteDataArray = new JSONArray();
        for (Note note : PrefsUtil.getNotes("notes", requireActivity())){
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

        for (Note note : PrefsUtil.getNotes("favorites", requireActivity())){
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
        JSONArray notesData = getDataAsJSONArray();
        String data = notesData.toString();
        Log.d("import_test", data);
        return data.getBytes();
    }

    private void saveFileToStorage() {
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
                OutputStream outputStream = requireActivity().getContentResolver().openOutputStream(uri);
                outputStream.write(createExportData());
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void exportNotes() {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startDialog();
        saveFileToStorage();
        loadingDialog.dismissDialog();
    }

    private void chooseFileForImport(){
        Intent target = new Intent(Intent.ACTION_GET_CONTENT);
        target.setType("application/*");
        target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        target.addCategory(Intent.CATEGORY_OPENABLE);
        Intent intent = Intent.createChooser(target, "Import File");
        startActivityForResult(intent, IMPORT_CODE);
    }

    private JSONArray readFile(Uri uri){
        String content = "";
        try {
            StringBuilder buffer = new StringBuilder();
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            if (inputStream != null){
                while ((content = reader.readLine()) != null){
                    buffer.append(content).append("\n");
                }
            }
            content = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray json = null;
        try {
            json = new JSONArray(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // Use the JSONArray's contents to import the notes
    private void parseFile(JSONArray content){
        if (content == null) return;
        for (int i = 0; i < content.length(); i++){
            try {
                JSONObject noteData = content.getJSONObject(i);
                Note importedNote = getNoteFromJSONData(noteData);
                if (importedNote != null) {
                    saveImportedNote(importedNote);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Check which list to add the imported note to
    private void saveImportedNote(Note note){
        ArrayList<Note> notes = PrefsUtil.getNotes("notes", requireActivity());
        ArrayList<Note> favorites = PrefsUtil.getNotes("favorites", requireActivity());
        if (note.getFavorite())
            favorites.add(note);
        else
            notes.add(note);

        PrefsUtil.saveNotes(notes, "notes", requireActivity());
        PrefsUtil.saveNotes(favorites, "favorites", requireActivity());
    }

    // Turn the JSON data into a Note object and return it
    private Note getNoteFromJSONData(JSONObject data) {
        try {
            return new Note(data.getString("title"),
                    data.getString("content"),
                    data.getInt("color"),
                    data.getString("date").replace("\\", ""),
                    data.getBoolean("favorite")
                    );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void importFile(Intent intent){
        if (intent == null) return;

        Uri uri = intent.getData();
        String fileName = GeneralUtil.getFileName(uri, getActivity());
        if (GeneralUtil.isValidFileType(fileName)){
            JSONArray fileContents = readFile(uri);
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
                    GeneralUtil.showShortToast("Exported notes", requireActivity());
                    break;
                case IMPORT_CODE:
                    importFile(resultData);
                    GeneralUtil.showShortToast("Imported notes", requireActivity());
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
