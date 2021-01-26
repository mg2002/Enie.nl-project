package com.e4all.main;

public class Achievement {
    private String title;
    private String description;
    private int level;
    private int[] milestones;
    private boolean achieved = false;

    public Achievement(int level, String title, String description, int[] milestones){
        this.title = title;
        this.description = description;
        this.level = level;
        this.milestones = milestones;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public int getLevel() {
        return level;
    }

    public int[] getMilestones(){
        return milestones;
    }

    public boolean getAchieved(){
        return achieved;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }
}
