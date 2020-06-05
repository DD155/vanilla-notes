package com.example.vanillanotes;

public class Note {
    private String title;
    private String text;

    public Note(String title, String text){
        this.title = title;
        this.text = text;
    }

    public Note(String text){
        this.title = "";
        this.text = text;
    }

    public String getTitle(){
        return title;
    }

    public String getText(){
        return text;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setText(String text){
        this.text = text;
    }
}
