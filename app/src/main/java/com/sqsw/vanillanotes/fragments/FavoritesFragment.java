package com.sqsw.vanillanotes.fragments;

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

import com.github.clans.fab.FloatingActionMenu;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.EditActivity;
import com.sqsw.vanillanotes.ItemClickSupport;
import com.sqsw.vanillanotes.model.Note;
import com.sqsw.vanillanotes.model.NotesAdapter;
import com.sqsw.vanillanotes.util.PrefsUtil;
import com.sqsw.vanillanotes.util.GeneralUtil;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FavoritesFragment extends Fragment {
    private View view;
    private ArrayList<Note> favs;
    private Context context;
    private GeneralUtil UTIL;
    private NotesAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionMenu fam;
    private int selectedSortItem = 4;
    private boolean isSearched;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notes_recycler_layout, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favorites");
        setHasOptionsMenu(true);
        if (isAdded()) context = getActivity();

        fam = getActivity().findViewById(R.id.fam);
        fam.setVisibility(View.VISIBLE);
        fam.setClosedOnTouchOutside(true);

        UTIL = new GeneralUtil(getActivity());
        favs = PrefsUtil.getNotes("favorites", context);
        recyclerView = view.findViewById(R.id.recycler_notes);
        adapter = new NotesAdapter(favs);

        if (favs.size() > 0)
            UTIL.sortNotes(selectedSortItem, favs, "notes");
        else {
            TextView defaultText = view.findViewById(R.id.clear_text);
            defaultText.setText(getResources().getString(R.string.favs_empty));
        }

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(listener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    ItemClickSupport.OnItemClickListener listener = new ItemClickSupport.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            Intent intent = new Intent(context, EditActivity.class);
            Note current = adapter.getItem(position);

            if (isSearched)
                intent.putExtra("index", getIndexOfSearchedNote(current));
            else
                intent.putExtra("index", position);

            intent.putExtra("oldNote", true);
            intent.putExtra("favorite", true);
            startActivity(intent);
            fam.close(true);
        }
    };

    private int getIndexOfSearchedNote(Note note){
        ArrayList<Note> notes = PrefsUtil.getNotes("favorites", context);
        for (int i = 0; i < notes.size(); i++)
            if (note.equals(notes.get(i)))
                return i;
        return 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fav_actions, menu);

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint_fav));

        myActionMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                Log.d("sv_test", "opened");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                isSearched = false;
                return true;
            }
        });

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
        if (item.getItemId() == R.id.action_sort){
            sortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortDialog() {
        final SharedPreferences prefs = context.getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        selectedSortItem = prefs.getInt("sort_index", 0);

        String[] items = getResources().getStringArray(R.array.sort_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogThemeLight);
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
                UTIL.sortNotes(selectedSortItem, favs, "favorites");
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
}
