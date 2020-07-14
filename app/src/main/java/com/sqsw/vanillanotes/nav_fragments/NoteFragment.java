package com.sqsw.vanillanotes.nav_fragments;

import android.animation.RectEvaluator;
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
import com.sqsw.vanillanotes.classes.DateComparator;
import com.sqsw.vanillanotes.classes.ItemClickSupport;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.activities.NoteEditActivity;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.NoteComparator;
import com.sqsw.vanillanotes.classes.NotesAdapter;
import com.sqsw.vanillanotes.classes.Utility;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class NoteFragment extends Fragment {
    private View view;
    private ArrayList<Note> notes;
    private SharedPreferences prefs;
    private Utility UTIL;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private int selectedSortItem = 4;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //view = inflater.inflate(R.layout.notes_layout, container, false);
        view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        final ArrayList<Note> starredList;
        UTIL = new Utility(getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notes");

        recyclerView = view.findViewById(R.id.recycler_notes);
        //Bundle bundle = getArguments();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //noteList = (ArrayList<Note>)bundle.getSerializable(SERIALIZABLE_KEY);
        notes = getNotes("notes");
        starredList = getNotes("starred");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        int sortValue = sharedPreferences.getInt("sort_index", 0);

        adapter = new NotesAdapter(notes);

        if (notes.size() > 0)
            sortNotes(sortValue);
        else {
            TextView defaultText = view.findViewById(R.id.clear_text);
            defaultText.setText(getResources().getString(R.string.notes_empty));
        }

        Log.d("sort_test", "3");

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent notesActivity = new Intent();
                notesActivity.setClass(getActivity(), NoteEditActivity.class);
                notesActivity.putExtra("oldNote", true);
                notesActivity.putExtra("index", position);
                startActivity(notesActivity);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        /*
        if (noteList.size() != 0) { // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(getContext());
                final int index = i; // Index of ArrayList
                final Note currNote = noteList.get(i);
                final Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, currNote.getColor());

                text.setBackground(drawable);
                initializeText(text, currNote);
                linear.addView(text);

                // Make the text clickable
                text.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()){
                            case MotionEvent.ACTION_CANCEL:
                                text.setBackground(drawable);
                                return true;

                            case MotionEvent.ACTION_DOWN:
                                if (currNote.getColor() != -1) {
                                    String newHex = UTIL.getDarkerColor(currNote.getColor());
                                    // Create new drawable to replace
                                    Drawable holdDrawable = UTIL.returnDrawable(R.drawable.shadow_border);
                                    holdDrawable.setColorFilter(new
                                            PorterDuffColorFilter(Color.parseColor(newHex), PorterDuff.Mode.MULTIPLY));
                                    text.setBackground(holdDrawable);
                                } else
                                    text.setBackgroundResource(R.drawable.shadow_border_hold);

                                return true;

                            case MotionEvent.ACTION_UP:
                                // Check if location of user touch is still within the TextView
                                if ((int)event.getX() >= 0 && (int)event.getX() <= 1360
                                        && (int)event.getY() >= 0 && (int)event.getY() <= 300){
                                    v.performClick();
                                    Intent notesActivity = new Intent();

                                    notesActivity.setClass(getActivity().getApplicationContext(), NoteEditActivity.class);
                                    notesActivity.putExtra("oldNote", true);
                                    notesActivity.putExtra("index", index); // pass index to next activity to change content later
                                    //notesActivity.putExtra("caller", "MainActivity");
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
        } */
        setHasOptionsMenu(true);

        return view;
    }

    // Create dialog for sorting notes
    private void sortDialog() {
        final SharedPreferences prefs = getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        if (selectedSortItem != prefs.getInt("sort_index", 0)){
            selectedSortItem = prefs.getInt("sort_index", 0);
        }

        String[] items = getResources().getStringArray(R.array.sort_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sort");
        builder.setCancelable(true);
        builder.setSingleChoiceItems(items, selectedSortItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                selectedSortItem = index;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("sort_index", index);
                editor.apply();
            }
        });

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("selected_index", selectedSortItem + "");
                dialogInterface.dismiss();
                sortNotes(selectedSortItem);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    // Function for sort based on user selection of previous dialog above
    // Type 4 = Custom Sort (User created sort)
    private void sortNotes(int type){
        switch (type){
            case 0:
                // Type 0 = Sort by Title (Ascending)
                Log.d("selected_index", "case 0");
                Collections.sort(notes, new NoteComparator());
                UTIL.saveNotes(notes, "notes");
                adapter.notifyDataSetChanged();
                break;
            case 1:
                // Type 1 = Sort by Title (Descending)
                Log.d("selected_index", "case 1");
                Collections.sort(notes, new NoteComparator());
                Collections.reverse(notes);
                UTIL.saveNotes(notes, "notes");
                adapter.notifyDataSetChanged();
                break;
            case 2:
                // Type 2 = Sort by Date Created (Ascending)
                Collections.sort(notes, new DateComparator());
                UTIL.saveNotes(notes, "notes");
                adapter.notifyDataSetChanged();
                break;
            case 3:
                // Type 3 = Sort by Date Created (Descending)
                Collections.sort(notes, new DateComparator());
                Collections.reverse(notes);
                UTIL.saveNotes(notes, "notes");
                adapter.notifyDataSetChanged();
                break;
            case 4:
                // TODO: Custom sort
                break;
        }
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

    // Creates dialog for the clear
    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.clear_notes_title));
        builder.setMessage(getString(R.string.clear_notes_text));
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

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Remove notes by clearing note ArrayList and resetting linear layout.
    private void clearNotes(){
        ArrayList<Note> trash = UTIL.getNotes("trash");
        trash.addAll(notes);
        int size = notes.size();
        notes.clear();

        UTIL.saveNotes(notes, "notes");
        UTIL.saveNotes(trash, "trash");

        adapter.notifyItemRangeRemoved(0, size);
        Toast.makeText(getActivity(), getString(R.string.clear_notes_toast), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.notes_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                UTIL.goToActivity(NoteEditActivity.class, null, getActivity());
                return true;

            case R.id.action_clear:
                createDialog();
                return true;

            case R.id.action_sort:
                sortDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
