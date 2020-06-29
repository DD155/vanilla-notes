package com.sqsw.vanillanotes.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.sqsw.vanillanotes.activities.MainActivity;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.TrashActivity;

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

        //adds setting fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_content,
                new SettingsFragment()).commit();

    }

    // determines which activity to go back to
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        Intent intent;
        if ("TrashActivity".equals(getIntent().getStringExtra("caller")))
            intent = new Intent(getApplicationContext(), TrashActivity.class);
        else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
        }

        startActivityForResult(intent, 0);
        return true;
    }

}
