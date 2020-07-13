package com.sqsw.vanillanotes.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sqsw.vanillanotes.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private Utility util;


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public View img;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);
            img = itemView.findViewById(R.id.colorview);
        }
    }

    private List<Note> notes;
    private Context context;

    public NotesAdapter(List<Note> notes){
        this.notes = notes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_note, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        util = new Utility(context);
        int fontSize = util.getFontSize(prefs.getString("font_size", null));
        Note note = notes.get(position);

        // Set title, content, and color of the note
        TextView title_tv = holder.title;
        title_tv.setText(note.getTitle());
        title_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize + 3);

        TextView content_tv = holder.content;
        content_tv.setText(note.getText());
        content_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        View view = holder.img;
        view.setBackgroundColor(note.getColor());

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


}
