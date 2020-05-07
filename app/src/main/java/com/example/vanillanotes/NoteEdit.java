package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

public class NoteEdit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        EditText text = findViewById(R.id.editText);
        text.setBackgroundResource(R.drawable.back);
    }

    public void saveText(View v){
        EditText text = findViewById(R.id.editText);
        String s = text.getText().toString();
        Intent prev = new Intent();
        prev.setClass(this, MainActivity.class);
        prev.putExtra("note", s);
        prev.putExtra("isSaved", true);
        startActivity(prev);
    }
}
