package com.sqsw.vanillanotes;

import com.sqsw.vanillanotes.model.Note;

import java.util.Comparator;

public class NoteComparator implements Comparator<Note> {
    public int compare(Note a, Note b){
        return a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase());
    }
}

