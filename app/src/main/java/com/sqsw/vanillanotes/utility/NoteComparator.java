package com.sqsw.vanillanotes.utility;

import com.sqsw.vanillanotes.note.Note;

import java.util.Comparator;

public class NoteComparator implements Comparator<Note> {
    public int compare(Note a, Note b){
        return a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase());
    }
}

