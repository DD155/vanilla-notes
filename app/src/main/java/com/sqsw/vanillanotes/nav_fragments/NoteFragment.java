package com.sqsw.vanillanotes.nav_fragments;

import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sqsw.vanillanotes.Note;
import com.sqsw.vanillanotes.NoteEditActivity;
import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.Utility;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.fragment.app.Fragment;

public class NoteFragment extends Fragment {
    private View view;
    private LinearLayout linear;
    private Helper helper;
    private static final String SERIALIZABLE_KEY = "KEY";
    private Utility UTIL;
    private FragmentListener listener;

    public interface FragmentListener {
        void onInputSent (CharSequence input);
    }

    // Get Notes ArrayList from Main Activity
    public static NoteFragment newInstance(ArrayList<Note> notes) {
        Bundle args = new Bundle();
        args.putSerializable(SERIALIZABLE_KEY, notes);
        NoteFragment fragment = new NoteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notes_layout, container, false);
        final ArrayList<Note> noteList; // Declare Notes ArrayList
        //SharedPreferences prefs = getContext().getSharedPreferences("NOTES", Context.MODE_PRIVATE);
        linear = view.findViewById(R.id.linear);
        UTIL = new Utility(getActivity().getApplicationContext());

        Bundle bundle = getArguments();

        Log.d("Frag_test", "clear 1");
        if (bundle != null){
            Log.d("Frag_test", "received info");
        } else {
            Log.d("Frag_test", "bundle is null");
        }

        noteList = (ArrayList<Note>)bundle.getSerializable(SERIALIZABLE_KEY);

        Log.d("Frag_test", "clear 2");


        if (noteList.size() != 0) { // Makes sure user has already notes, loads them on entering app
            for (int i = 0; i < noteList.size(); i++) {
                final TextView text = new TextView(getContext());
                final Note currNote = noteList.get(i);
                Log.d("Frag_test", "clear 3");
                String title = currNote.getTitle();
                String description = currNote.getText();
                Log.d("Frag_test", "clear 4");

                final Drawable drawable = UTIL.changeDrawableColor(R.drawable.shadow_border, currNote.getColor());
                text.setBackground(drawable);
                Log.d("Frag_test", "clear 5");

                /*
                String[] strParts = description.split("\\r?\\n|\\r");

                Log.d("length", Integer.toString(strParts[0].length()));
                if (strParts[0].length() >= 82){
                    description = util.addEllipsis(strParts[0]);
                    Log.d("length", Integer.toString(description.length()));
                } else if (util.countLines(description) > 2){
                    // Create array of the text without any new lines
                    description = strParts[0] + "\n" + util.addEllipsis(strParts[1]); // Concatenate everything
                } */


                // Make the title larger than the description
                SpannableString str = new SpannableString(title + "\n" + description);
                str.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                Log.d("Frag_test", "clear 6");

                initializeText(text, currNote.getColor());
                text.setText(str);
                linear.addView(text);
                Log.d("Frag_test", "clear 7");
                //final Intent notesActivity = new Intent();

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
                                    String[] rgbStr = {(UTIL.hexFromColorInt(currNote.getColor())).substring(0, 2),
                                            (UTIL.hexFromColorInt(currNote.getColor())).substring(2, 4),
                                            (UTIL.hexFromColorInt(currNote.getColor())).substring(4)
                                    };
                                    double[] rgb = { // Divide RGB value to make the result darker
                                            Math.round(Integer.valueOf(rgbStr[0], 16) * 0.75),
                                            Math.round(Integer.valueOf(rgbStr[1], 16) * 0.75),
                                            Math.round(Integer.valueOf(rgbStr[2], 16) * 0.75)
                                    };
                                    // Format string in #RRGGBB style
                                    String newHex = String.format("#%02X%02X%02X", (int)rgb[0], (int)rgb[1], (int)rgb[2]);

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

                                    notesActivity.setClass(getActivity().getApplicationContext(), NoteEditActivity.class);
                                    notesActivity.putExtra("savedText", noteList.get(index).getText());
                                    notesActivity.putExtra("savedTitle", noteList.get(index).getTitle());
                                    notesActivity.putExtra("index", index); // pass index to next activity to change content later
                                    notesActivity.putExtra("caller", "MainActivity");
                                    notesActivity.putExtra("date", noteList.get(index).getDate());
                                    notesActivity.putExtra("color", currNote.getColor());
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
        }
        return view;
    }

    private void initializeText(TextView text, int color){
        float density = getResources().getDisplayMetrics().density;
        int fontSize = UTIL.getFontSize(getActivity().getSharedPreferences("NOTES", Context.MODE_PRIVATE).getString("font_size", ""));
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
        if (UTIL.isDarkColor(color))
            text.setTextColor(getResources().getColor(R.color.white));
        else text.setTextColor(getResources().getColor(R.color.textColor));
    }
}
