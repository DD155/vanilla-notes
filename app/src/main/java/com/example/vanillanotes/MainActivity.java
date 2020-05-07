package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<String> textList;
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        final LinearLayout linear = findViewById(R.id.linear);
        final Intent goToSecond = new Intent();


        //editor.remove("textStrings");
        //editor.commit();

        if (prefs.contains("textStrings")) {
            Log.d("myTag", "textStrings is valid.");
            textList = getArrayList("textStrings");
        }
        else{
            textList = new ArrayList<String>();
        }


        // information from edited note activity
        Intent caller = getIntent();
        final String editedText = caller.getStringExtra("note");
        //boolean saved = caller.getBooleanExtra("isSaved", false);

        /*
        if (prefs.contains("textStrings")) {
            textList = getArrayList("textStrings");
            Log.d("myTag", "Size: "+textList.size());
        }
        */

        if (editedText != null){
            Log.d("myTag", "not null");
            textList.add(editedText);
            saveArrayList(textList, "textStrings");
        }

        if (textList.size() != 0){
            Log.d("myTag", "array valid");
            Log.d("myTag", textList.size()+"");
            for (int i = 0; i < textList.size(); i++) {
                TextView text = new TextView(this);
                Log.d("myTag", textList.get(i));
                initializeText(text);
                text.setText(textList.get(i));
                linear.addView(text);
                // make the text clickable
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // clicked text sends user to edit the note
                        goToSecond.setClass(getApplicationContext(), NoteEdit.class);
                        startActivity(goToSecond);
                    }
                });
            }
        }

        Button b = findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // used to create clickable text
                //textViewList.add(text);

                goToSecond.setClass(getApplicationContext(), NoteEdit.class);
                startActivity(goToSecond);

                //linear.addView(textViewList.get(textViewList.size()-1));
            }
        });
    }

    public void initializeText(TextView text){
        text.setTextSize(15);
        text.setBackgroundResource(R.drawable.back);
        text.setWidth(1500);
        text.setPadding(5, 70, 5, 70);
    }

    public void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

}
