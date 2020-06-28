package com.sqsw.vanillanotes;

import java.util.Comparator;

class NoteComparator implements Comparator<Note> {

    public int compare(Note a, Note b){
        return a.getTitle().compareTo(b.getTitle());
    }
}


