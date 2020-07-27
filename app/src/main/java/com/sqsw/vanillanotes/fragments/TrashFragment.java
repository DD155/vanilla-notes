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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TrashFragment extends Fragment {
    private View view;
    private ArrayList<Note> noteList;
    private Utility UTIL;
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private boolean isSearched = false;
    private int selectedSortItem = 4;


    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Trash");

        getActivity().findViewById(R.id.fam).setVisibility(View.GONE);

        view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        UTIL = new Utility(getActivity().getApplicationContext());

        Intent def = new Intent();
        def.putExtra("caller", "Trash");
        noteList = getNotes("trash");
        recyclerView = view.findViewById(R.id.recycler_notes);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(requireActivity(), NoteEditActivity.class);
                Note current = adapter.getItem(position);

                if (isSearched) {
                    for (int i = 0; i < getNotes("trash").size(); i++) {
                        if (current.equals(getNotes("trash").get(i))) {
                            intent.putExtra("index", i);
                            break;
                        }
                    }
                } else {
                    intent.putExtra("index", position);
                }
                intent.putExtra("oldNote", true);
                intent.putExtra("caller", "Trash");
                startActivity(intent);
                requireActivity().finish();
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

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView)myActionMenuItem.getActionView();
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
        } else if (item.getItemId() == R.id.action_sort){ // Case where user selects sort
            sortDialog();
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
