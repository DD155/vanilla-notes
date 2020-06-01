package com.example.vanillanotes.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.vanillanotes.MainActivity;
import com.example.vanillanotes.NoteEditActivity;
import com.example.vanillanotes.R;
import com.example.vanillanotes.TrashActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //add toolbar

        Toolbar myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Settings");
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        getSupportFragmentManager().beginTransaction().replace(R.id.settings_content,
                new SettingsFragment()).commit();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent;
        if (getIntent().getStringExtra("caller").equals("MainActivity"))
            intent = new Intent(getApplicationContext(), MainActivity.class);
        else if (getIntent().getStringExtra("caller").equals("TrashActivity"))
            intent = new Intent(getApplicationContext(), TrashActivity.class);
        else {
            intent = new Intent(getApplicationContext(), NoteEditActivity.class);
        }

        startActivityForResult(intent, 0);
        return true;
    }

}
