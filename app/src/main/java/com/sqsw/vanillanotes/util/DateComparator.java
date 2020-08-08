package com.sqsw.vanillanotes.util;

import android.util.Log;

import com.sqsw.vanillanotes.note.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class DateComparator implements Comparator<Note> {
    @Override
    public int compare(Note a, Note b) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Log.d("date_test", "First note date: " + a.getDate().substring(0, a.getDate().length() - 3)
                + " Second note date: " + b.getDate().substring(0, b.getDate().length() - 3));
        Date first, second;
        try {
            first = format.parse(a.getDate().substring(0, a.getDate().length() - 3));
            second = format.parse(b.getDate().substring(0, b.getDate().length() - 3));

            Log.d("date_test", "converted date 1: " + first + " converted date 2 : " + second);
            //Log.d("date_test", "Chosen compared date: "+first.compareTo(second));
            return first.compareTo(second);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return a.getDate().compareTo(b.getDate());
    }
}
