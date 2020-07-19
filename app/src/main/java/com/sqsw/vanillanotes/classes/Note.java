package com.sqsw.vanillanotes.classes;

import android.graphics.Color;

import java.io.Serializable;

public class Note implements Serializable {
    private boolean favorite;
    private String title;
    private String text;
    private String date;
    private int color;
    private int index = -1;

    public Note(String title, String text, int color, String date){
        this.title = title;
        this.text = text;
        this.date = date;
        this.color = color;
        this.favorite = false;
    }

    public Note(String title, String text, int color){
        this.title = title;
        this.text = text;
        this.date = "";
        this.color = color;
        this.favorite = false;
    }

    public Note(String title, String text){
        this.title = title;
        this.text = text;
        this.date = "";
        this.color = Color.WHITE;
        this.favorite = false;
    }

    public Note(String text){
        this.title = "";
        this.text = text;
        this.date = "";
        this.color = Color.WHITE;
        this.favorite = false;
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
