package com.sqsw.vanillanotes.classes;

import com.sqsw.vanillanotes.classes.Note;

import java.util.Comparator;

public class NoteComparator implements Comparator<Note> {
    public int compare(Note a, Note b){
        return a.getTitle().compareTo(b.getTitle());
    }
}

