package com.sqsw.vanillanotes;

import java.util.Comparator;

class SortTitleAscending implements Comparator<Note> {

    public int compare(Note a, Note b){
        return a.getTitle().compareTo(b.getTitle());
    }
}

class SortTitleDescending implements Comparator<Note> {

    public int compare(Note a, Note b){
        return a.getTitle().compareTo(b.getTitle());
    }
}


