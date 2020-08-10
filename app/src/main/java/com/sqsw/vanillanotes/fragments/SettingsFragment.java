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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingsFragment extends PreferenceFragmentCompat {

    Activity mActivity;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private final String KEY_FONT = "font_size";
    private final String KEY_BACK_DIALOG = "back_dialog_toggle";
    private final int PERMISSION_CODE = 1;
    private final int REQUEST_CODE = 5;

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
                    requestStoragePermissions();
                } else {
                    exportFiles();
                }
                return false;
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

    private byte[] createExportData(){
        String data = "";
        for (Note note : PrefsUtil.getNotes("notes", getActivity())){
            data += "[VN]Title:" + note.getTitle()
                    + "\n[VN]Content:" + note.getContent()
                    + "\n[VN]Color:" + note.getColor()
                    + "\n[VN]Favorite:" + note.getFavorite()
                    + "\n";
        }

        for (Note note : PrefsUtil.getNotes("favorites", getActivity())){
            data += "[VN]Title:" + note.getTitle()
                    + "\n[VN]Content:" + note.getContent()
                    + "\n[VN]Color:" + note.getColor()
                    + "\n[VN]Favorite:" + note.getFavorite()
                    + "\n\n";
        }
        return data.getBytes();
    }

    private void exportFiles() {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startDialog();
        byte[] data = createExportData();

        try {
            openFileChooser(data);
            loadingDialog.dismissDialog();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void openFileChooser(byte[] data) throws IOException {
        String date;
        Calendar calendar = Calendar.getInstance();
        date = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH)
                + "-" + calendar.get(Calendar.DAY_OF_MONTH);

        Intent target = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        target.setType("text/*");
        target.putExtra(Intent.EXTRA_TITLE, "vanillanotes-" + date + ".txt");
        Intent intent = Intent.createChooser(target, "Save File");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri);
                    outputStream.write(createExportData());
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale
                (getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(getActivity(), R.style.DialogThemeLight)
                    .setTitle(getString(R.string.perm_dialog_title))
                    .setMessage(getString(R.string.perm_dialog_msg))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                    , PERMISSION_CODE);
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
