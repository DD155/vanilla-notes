package com.sqsw.vanillanotes.classes;

import java.io.Serializable;

public class Note implements Serializable {
    private String title;
    private String text;
    private String date;
    private boolean favorite;
    private int color;
    private int index; // TODO: Implement indexing for custom user sort

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
        this.color = -1;
        this.favorite = false;
    }

    public Note(String text){
        this.title = "";
        this.text = text;
        this.date = "";
        this.color = -1;
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

    public boolean getFavorite(){
        return favorite;
    }



}
