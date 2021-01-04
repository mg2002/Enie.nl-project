package com.e4all.main;

import javafx.scene.image.Image;

public class Achievement {
    private Image badge;
    private String title;
    private String description;

    public Achievement(Image badge, String title, String description){
        this.title = title;
        this.description = description;
        this.badge = badge;
    }

    public void drawAchievement(){

    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }
}
