package com.example.vanillanotes.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import android.os.Bundle;

import com.example.vanillanotes.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Settings");
        setSupportActionBar(myToolbar);

        //add settings
        getSupportFragmentManager().beginTransaction().replace(R.id.setting_content,
                new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.root_preferences);
        }
    }
}
