package com.sqsw.vanillanotes.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.sqsw.vanillanotes.R;

public class ContextActionBar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_action_bar);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("");

        tb.inflateMenu(R.menu.hold_notes_actions);
        //tb.setOnMenuItemClickListener();


    }
}