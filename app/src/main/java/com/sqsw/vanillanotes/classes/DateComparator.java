package com.sqsw.vanillanotes.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class DateComparator implements Comparator<Note> {
    @Override
    public int compare(Note a, Note b) {
        SimpleDateFormat format = new SimpleDateFormat("M/D/YYYY HH:MM:SS");
        Date first, second;
        try {
            first = format.parse(a.getDate().substring(0, a.getDate().length() - 3));
            second = format.parse(b.getDate().substring(0, b.getDate().length() - 3));
            return first.compareTo(second);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return a.getDate().compareTo(b.getDate());
    }
}
