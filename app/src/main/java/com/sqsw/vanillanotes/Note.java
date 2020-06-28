package com.sqsw.vanillanotes;

public class Note {
    private String title;
    private String text;
    private String date;
    private int color;
    private int index; // TODO: Implement indexing for custom user sort

    public Note(String title, String text, int color, String date){
        this.title = title;
        this.text = text;
        this.date = date;
        this.color = color;
    }

    public Note(String title, String text, int color){
        this.title = title;
        this.text = text;
        this.date = "";
        this.color = color;
    }

    public Note(String title, String text){
        this.title = title;
        this.text = text;
        this.date = "";
        this.color = -1;
    }

    public Note(String text){
        this.title = "";
        this.text = text;
        this.date = "";
        this.color = -1;
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



}
