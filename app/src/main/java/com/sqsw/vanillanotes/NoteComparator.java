package com.sqsw.vanillanotes;

import java.util.Comparator;

class NoteComparator implements Comparator<Note> {
    public int compare(Note a, Note b){
        return a.getTitle().compareTo(b.getTitle());
    }
}

class DateComparator implements Comparator<Note>{
    @Override
    public int compare(Note a, Note b) {
        return a.getDate().compareTo(b.getDate());
    }
}
