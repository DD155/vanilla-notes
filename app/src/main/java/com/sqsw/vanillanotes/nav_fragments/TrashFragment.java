package com.sqsw.vanillanotes.nav_fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.NoteEditActivity;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.classes.Utility;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class TrashFragment extends Fragment {
    private View view;
    private LinearLayout linear;
    private ArrayList<Note> noteList;
    private SharedPreferences prefs;
    private Utility UTIL;


    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Trash");

        view = inflater.inflate(R.layout.notes_layout, container, false);
        linear = view.findViewById(R.id.linear);
        UTIL = new Utility(getActivity().getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Intent def = new Intent();
        def.putExtra("caller", "Trash");
        noteList = getNotes("trash");

        if (noteList.size() != 0) { // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(getContext());
                final Note currNote = noteList.get(i);

                final Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, currNote.getColor());
                text.setBackground(drawable);

                initializeText(text, currNote);
                linear.addView(text);

                // Make the text clickable
                final int index = i; // Index of ArrayList as,dlas

                text.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()){
                            case MotionEvent.ACTION_CANCEL:
                                Log.d("cancel_action", "Cancel action");
                                text.setBackground(drawable);
                                return true;

                            case MotionEvent.ACTION_DOWN:
                                if (currNote.getColor() != -1) {
                                    // Logic for making pressed down color a darker shade
                                    String newHex = UTIL.getDarkerColor(currNote.getColor());
                                    // Create new drawable to replace
                                    Drawable holdDrawable = UTIL.returnDrawable(R.drawable.shadow_border);
                                    holdDrawable.setColorFilter(new
                                            PorterDuffColorFilter(Color.parseColor(newHex), PorterDuff.Mode.MULTIPLY));

                                    text.setBackground(holdDrawable);

                                    Log.d("shade", "New Hex: " + newHex);
                                } else
                                    text.setBackgroundResource(R.drawable.shadow_border_hold);

                                return true;

                            case MotionEvent.ACTION_UP:
                                // Check if location of user touch is still within the TextView
                                if ((int)event.getX() >= 0 && (int)event.getX() <= 1360
                                        && (int)event.getY() >= 0 && (int)event.getY() <= 300){
                                    v.performClick();
                                    Intent notesActivity = new Intent();

                                    if (getActivity() != null)
                                        notesActivity.setClass(getActivity().getApplicationContext(), NoteEditActivity.class);
                                    else {
                                        Log.e("null_err", "TrashFragment getActivity() in OnCreate() returns null");
                                    }
                                    notesActivity.putExtra("oldNote", true);
                                    notesActivity.putExtra("index", index); // pass index to next activity to change content later
                                    notesActivity.putExtra("caller", "Trash");
                                    text.setBackground(drawable);
                                    startActivity(notesActivity);
                                } else {
                                    // Just change the color back if user moves finger out of the textbox
                                    text.setBackground(drawable);
                                }
                                return true;
                        }
                        return false;
                    }
                });
            }
        } else {
            TextView defaultText = new TextView(getActivity().getApplicationContext());
            defaultText.setText(getResources().getString(R.string.trash_empty));
            defaultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            defaultText.setGravity(Gravity.CENTER_HORIZONTAL);
            linear.addView(defaultText);
        }
        setHasOptionsMenu(true);
        return view;
    }

    private void initializeText(TextView text, Note note){
        float density = getResources().getDisplayMetrics().density;
        int fontSize = UTIL.getFontSize(prefs.getString("font_size", null));
        int height;
        Log.d("density", Float.toString(density));
        // Set height based on dpi
        if (density >= 4.0) {
            height = 350;
            Log.d("density", "Density is 4.0");
        } else if (density >= 3.0) {
            height = 300;
            Log.d("density", "Density is 3.0");
        } else if (density >= 2.0) {
            height = 150;
            Log.d("density", "Density is 2.0");

        } else if (density >= 1.5) {
            height = 100;
            Log.d("density", "Density is 1.5");
        } else
        {
            height = 75;
            Log.d("density", "Density is 1.0");
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 25, 0, 25);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        text.setFilters(new InputFilter[] { new InputFilter.LengthFilter(82) });
        //text.setBackgroundResource(R.drawable.shadow_border);
        text.setHeight(height);
        text.setPadding(50, 20, 50, 30);
        text.setLayoutParams(params);

        // Make the title larger than the description
        SpannableString str = new SpannableString(note.getTitle() + "\n" + note.getText());
        str.setSpan(new RelativeSizeSpan(1.3f), 0, note.getTitle().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, note.getTitle().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setElevation(10);
        text.setText(str);

        if (UTIL.isDarkColor(note.getColor()))
            text.setTextColor(getResources().getColor(R.color.white));
        else text.setTextColor(getResources().getColor(R.color.textColor));
    }

    private void confirmDialog(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Empty Trash");
        builder.setMessage(getResources().getString(R.string.trash_clear_confirm));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearNotes();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void clearNotes(){
        noteList.clear();

        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(noteList);
            editor.putString("trash", json);
            editor.apply();
        } else Log.e("null_err", "TrashFragment getActivity() in clearNotes() returns null");

        LinearLayout ll = view.findViewById(R.id.linear);
        ll.removeAllViews();

        TextView defaultText = new TextView(getActivity());
        defaultText.setText(getResources().getString(R.string.trash_empty));
        defaultText.setTextSize(20);
        defaultText.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(defaultText);

        Toast.makeText(getActivity().getApplicationContext(), "Trash emptied", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.trash_actions, menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_empty) {
            if (noteList.size() != 0)
                confirmDialog();
            else {  // Case where the trash is already empty
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.clear_error_title);
                builder.setMessage(getResources().getString(R.string.trash_error));
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                android.app.AlertDialog alert = builder.create();
                alert.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Returns the ArrayList from sharedprefs
    public ArrayList<Note> getNotes(String key){
        SharedPreferences prefs = getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        if (gson.fromJson(json, type) == null) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, type);
    }
}
