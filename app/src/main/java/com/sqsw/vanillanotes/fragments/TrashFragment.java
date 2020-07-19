package com.sqsw.vanillanotes.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.NoteEditActivity;
import com.sqsw.vanillanotes.classes.ItemClickSupport;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.classes.NotesAdapter;
import com.sqsw.vanillanotes.classes.Utility;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TrashFragment extends Fragment {
    private View view;
    private ArrayList<Note> noteList;
    private SharedPreferences prefs;
    private Utility UTIL;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private int selectedSortItem = 4;


    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Trash");

        view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        UTIL = new Utility(getActivity().getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Intent def = new Intent();
        def.putExtra("caller", "Trash");
        noteList = getNotes("trash");
        recyclerView = view.findViewById(R.id.recycler_notes);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent notesActivity = new Intent();

                notesActivity.setClass(requireActivity(), NoteEditActivity.class);
                notesActivity.putExtra("oldNote", true);
                notesActivity.putExtra("index", position);
                notesActivity.putExtra("caller", "Trash"); // Pass caller to edit activity
                startActivity(notesActivity);
            }
        });

        if (noteList.size() > 0) {
            adapter = new NotesAdapter(noteList);
            UTIL.sortNotes(getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE).getInt("sort_index", 0),
                    noteList, "trash");
            adapter.notifyDataSetChanged();
        } else { // Show text showing the trash is empty
            TextView defaultText = view.findViewById(R.id.clear_text);
            defaultText.setText(getResources().getString(R.string.trash_empty));
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        /*
        if (noteList.size() != 0) { // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(getContext());
                final Note currNote = noteList.get(i);

                final Drawable drawable = UTIL.changeDrawableColor(R.drawable.note_background, currNote.getColor());
                text.setBackground(drawable);

                initializeText(text, currNote);
                //linear.addView(text);

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
                                    Drawable holdDrawable = UTIL.returnDrawable(R.drawable.note_background);
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
            //linear.addView(defaultText);
        } */
        setHasOptionsMenu(true);
        return view;
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
        int size = noteList.size();
        noteList.clear();

        adapter.notifyItemRangeRemoved(0, size);

        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(noteList);
            editor.putString("trash", json);
            editor.apply();
        } else Log.e("null_err", "TrashFragment getActivity() in clearNotes() returns null");

        TextView defaultText = view.findViewById(R.id.clear_text);
        defaultText.setText(getResources().getString(R.string.trash_empty));

        Toast.makeText(getActivity().getApplicationContext(), "Trash emptied", Toast.LENGTH_LONG).show();
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
                UTIL.sortNotes(selectedSortItem, noteList, "trash");
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
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
        } else { // Case where user selects sort
            sortDialog();
            return true;
        }
        //return super.onOptionsItemSelected(item);
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
