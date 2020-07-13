package com.sqsw.vanillanotes.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sqsw.vanillanotes.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public View img;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);
            img = itemView.findViewById(R.id.image_view);
        }
    }

    private List<Note> notes;

    public NotesAdapter(List<Note> notes){
        this.notes = notes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_note, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);

        TextView title_tv = holder.title;
        title_tv.setText(note.getTitle());

        TextView content_tv = holder.content;
        content_tv.setText(note.getText());

        View view = holder.img;
        view.setBackgroundColor(note.getColor());

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


}
