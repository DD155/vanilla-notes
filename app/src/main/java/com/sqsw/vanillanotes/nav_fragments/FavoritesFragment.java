package com.sqsw.vanillanotes.nav_fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favorites");

        UTIL = new Utility(getActivity());
        favs = getNotes("favorites");

        Log.d("fav_test", favs.size() + "");

        recyclerView = view.findViewById(R.id.recycler_notes);
        //Bundle bundle = getArguments();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //noteList = (ArrayList<Note>)bundle.getSerializable(SERIALIZABLE_KEY);
        //favs = getNotes("notes");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        int sortValue = sharedPreferences.getInt("sort_index", 0);

        adapter = new NotesAdapter(favs);

        if (favs.size() == 0) {
            TextView defaultText = view.findViewById(R.id.clear_text);
            defaultText.setText(getResources().getString(R.string.favs_empty));
        }

        Log.d("sort_test", "3");

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent notesActivity = new Intent();
                notesActivity.setClass(getActivity(), NoteEditActivity.class);
                notesActivity.putExtra("oldNote", true);
                notesActivity.putExtra("index", position);
                notesActivity.putExtra("favorite", true);
                startActivity(notesActivity);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
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
