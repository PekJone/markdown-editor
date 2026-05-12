package com.markdown.editor.dto;

public class WeeklySummaryVO {
    
    private int weekArticles;
    private int weekViews;
    private int weekLikes;
    private int weekFollowers;

    public WeeklySummaryVO() {
    }

    public int getWeekArticles() {
        return weekArticles;
    }

    public void setWeekArticles(int weekArticles) {
        this.weekArticles = weekArticles;
    }

    public int getWeekViews() {
        return weekViews;
    }

    public void setWeekViews(int weekViews) {
        this.weekViews = weekViews;
    }

    public int getWeekLikes() {
        return weekLikes;
    }

    public void setWeekLikes(int weekLikes) {
        this.weekLikes = weekLikes;
    }

    public int getWeekFollowers() {
        return weekFollowers;
    }

    public void setWeekFollowers(int weekFollowers) {
        this.weekFollowers = weekFollowers;
    }
}
