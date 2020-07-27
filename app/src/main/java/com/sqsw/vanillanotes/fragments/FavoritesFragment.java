package com.sqsw.vanillanotes.fragments;

import android.content.Context;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FavoritesFragment extends Fragment {
    private View view;
    private ArrayList<Note> favs;
    private Utility UTIL;
    private NotesAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isSearched = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favorites");
        getActivity().findViewById(R.id.fam).setVisibility(View.VISIBLE);

        UTIL = new Utility(getActivity());
        favs = getNotes("favorites");

        Log.d("fav_test", favs.size() + "");

        recyclerView = view.findViewById(R.id.recycler_notes);
        adapter = new NotesAdapter(favs);

        if (favs.size() == 0) {
            TextView defaultText = view.findViewById(R.id.clear_text);
            defaultText.setText(getResources().getString(R.string.favs_empty));
        }

        Log.d("sort_test", "3");

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(requireActivity(), NoteEditActivity.class);
                Note current = adapter.getItem(position);

                if (isSearched) {
                    for (int i = 0; i < getNotes("favorites").size(); i++) {
                        if (current.equals(getNotes("favorites").get(i))) {
                            intent.putExtra("index", i);
                            break;
                        }
                    }
                } else {
                    intent.putExtra("index", position);
                }
                intent.putExtra("oldNote", true);
                intent.putExtra("favorite", true);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fav_actions, menu);

        // Initialize the searchview in the toolbar
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
