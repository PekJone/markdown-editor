package com.markdown.editor.dto;

public class FollowStatsVO {
    
    private int totalFollowers;
    private int monthlyGrowth;
    private int dailyGrowth;

    public FollowStatsVO() {
    }

    public int getTotalFollowers() {
        return totalFollowers;
    }

    public void setTotalFollowers(int totalFollowers) {
        this.totalFollowers = totalFollowers;
    }

    public int getMonthlyGrowth() {
        return monthlyGrowth;
    }

    public void setMonthlyGrowth(int monthlyGrowth) {
        this.monthlyGrowth = monthlyGrowth;
    }

    public int getDailyGrowth() {
        return dailyGrowth;
    }

    public void setDailyGrowth(int dailyGrowth) {
        this.dailyGrowth = dailyGrowth;
    }
}
