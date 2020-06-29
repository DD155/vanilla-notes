package com.sqsw.vanillanotes.classes;

import java.util.Comparator;

public class DateComparator implements Comparator<Note> {
    @Override
    public int compare(Note a, Note b) {
        return a.getDate().compareTo(b.getDate());
    }
}
