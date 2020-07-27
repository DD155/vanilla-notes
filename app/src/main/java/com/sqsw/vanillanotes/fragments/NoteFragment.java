package com.sqsw.vanillanotes.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

import com.github.clans.fab.FloatingActionMenu;
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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NoteFragment extends Fragment {
    private ArrayList<Note> notes;
    private SearchView searchView;
    private Utility UTIL;
    private RecyclerView recyclerView;
    private boolean isSearched = false;
    private NotesAdapter adapter;
    private int selectedSortItem = 4;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //view = inflater.inflate(R.layout.notes_layout, container, false);
        View view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        UTIL = new Utility(requireActivity());
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Notes");

        FloatingActionMenu fam = requireActivity().findViewById(R.id.fam);
        fam.setVisibility(View.VISIBLE);
        fam.setClosedOnTouchOutside(true);

        recyclerView = view.findViewById(R.id.recycler_notes);
        notes = getNotes("notes");


        Log.d("search_test", "Original size: " + notes.size());

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        int sortValue = sharedPreferences.getInt("sort_index", 0);

        if (notes.size() > 0) {
            UTIL.sortNotes(sortValue, notes, "notes");
        }
        else {
            TextView defaultText = view.findViewById(R.id.clear_text);
            defaultText.setText(getResources().getString(R.string.notes_empty));
        }

        adapter = new NotesAdapter(notes);
        adapter.notifyDataSetChanged();

        // Create onclick listener for RecyclerView items
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(listener);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setHasOptionsMenu(true);

        return view;
    }

    ItemClickSupport.OnItemClickListener listener = new ItemClickSupport.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            Intent intent = new Intent(requireActivity(), NoteEditActivity.class);
            Note current = adapter.getItem(position);

            if (isSearched) {
                for (int i = 0; i < getNotes("notes").size(); i++) {
                    if (current.equals(getNotes("notes").get(i))) {
                        intent.putExtra("index", i);
                        break;
                    }
                }
            } else {
                intent.putExtra("index", position);
            }

            intent.putExtra("oldNote", true);
            startActivity(intent);
        }
    };

    // Create dialog for sorting notes
    private void sortDialog() {
        final SharedPreferences prefs = requireActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        if (selectedSortItem != prefs.getInt("sort_index", 0)){
            selectedSortItem = prefs.getInt("sort_index", 0);
        }

        String[] items = getResources().getStringArray(R.array.sort_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
                UTIL.sortNotes(selectedSortItem, notes, "notes");
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    // Creates dialog for the clear
    private void createDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
                dialog.dismiss();
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

        // Initialize the searchview in the toolbar
        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapter.getFilter().filter(text);
                isSearched = true;

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        if (gson.fromJson(json, type) == null) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, type);
    }
}
