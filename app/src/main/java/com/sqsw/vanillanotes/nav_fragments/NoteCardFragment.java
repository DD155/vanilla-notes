package com.sqsw.vanillanotes.nav_fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.classes.Note;
import com.sqsw.vanillanotes.classes.Utility;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class NoteCardFragment extends Fragment {
    private View view;
    private LinearLayout linear;
    private SharedPreferences prefs;
    private Utility UTIL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notes_layout, container, false);
        final ArrayList<Note> noteList, starredList;
        linear = view.findViewById(R.id.linear);
        UTIL = new Utility(getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notes");
        //Bundle bundle = getArguments();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //noteList = (ArrayList<Note>)bundle.getSerializable(SERIALIZABLE_KEY);
        noteList = getNotes("notes");
        starredList = getNotes("starred");

        if (noteList.size() != 0) { // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                Note current = noteList.get(i);

                LinearLayout cardContent = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardContent.setOrientation(LinearLayout.VERTICAL);
                CardView card = new CardView(getActivity());

                TextView title = new TextView(getActivity());
                title.setPadding(30, 10, 30, 0);

                title.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Title);
                title.setLayoutParams(params);
                //title.setGravity(Gravity.CENTER);
                title.setText(current.getTitle());

                TextView content = new TextView(getActivity());
                content.setPadding(40, 0, 0, 20);

                content.setText(current.getText());

                cardContent.addView(title);
                cardContent.addView(content);

                card.addView(cardContent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    card.setElevation(8);
                } else {
                    card.setMaxCardElevation(8);
                }

                card.setRadius(25f);
                params.setMargins(0, 20, 0, 20);
                //params.height = 300;
                card.setLayoutParams(params);

                linear.addView(card);

            }
        }
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
