package com.sqsw.vanillanotes.note;

import android.graphics.Color;

import com.sqsw.vanillanotes.util.Utility;

import java.io.Serializable;

public class Note implements Serializable {
    private boolean favorite;
    private String title;
    private String text;
    private String date;
    private int color;

    public static Note createWithFavorite(String title, String text, boolean favorite){
        return new Note(title, text, Color.WHITE, Utility.currentDate(), favorite);
    }

    public static Note createWithTitleAndContent(String title, String content){
        return new Note(title, content, Color.WHITE, Utility.currentDate(), false);
    }

    public static Note createWithContent(String content){
        return new Note("", content, Color.WHITE, Utility.currentDate(), false);
    }

    public Note(String title, String text, int color, String date, boolean favorite){
        this.title = title;
        this.text = text;
        this.date = date;
        this.color = color;
        this.favorite = favorite;
    }

    public String getTitle(){
        return title;
    }

    public String getText(){
        return text;
    }

    public String getDate() { return date; }

    public int getColor() { return color; }


    public void setTitle(String title){
        this.title = title;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!Note.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Note b = (Note) obj;

        return (this.getTitle().equals(b.getTitle()) && this.getText().equals(b.getText()) &&
                this.getDate().equals(b.getDate()) && this.getColor() == b.getColor());
    }





}
