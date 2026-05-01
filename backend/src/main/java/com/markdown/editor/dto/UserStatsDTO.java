package com.markdown.editor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserStatsDTO {
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("nickname")
    private String nickname;
    @JsonProperty("avatar")
    private String avatar;
    @JsonProperty("original")
    private int original;
    @JsonProperty("likes")
    private int likes;
    @JsonProperty("collections")
    private int collections;
    @JsonProperty("comments")
    private int comments;
    @JsonProperty("blogRank")
    private int blogRank;
    @JsonProperty("followers")
    private int followers;
    @JsonProperty("following")
    private int following;
    @JsonProperty("continuousDays")
    private int continuousDays;
    
    @JsonProperty("continuousDaysDisplay")
    private String continuousDaysDisplay;
    
    @JsonProperty("views")
    private int views;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getOriginal() {
        return original;
    }

    public void setOriginal(int original) {
        this.original = original;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getCollections() {
        return collections;
    }

    public void setCollections(int collections) {
        this.collections = collections;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getBlogRank() {
        return blogRank;
    }

    public void setBlogRank(int blogRank) {
        this.blogRank = blogRank;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public int getContinuousDays() {
        return continuousDays;
    }

    public void setContinuousDays(int continuousDays) {
        this.continuousDays = continuousDays;
    }

    public String getContinuousDaysDisplay() {
        return continuousDaysDisplay;
    }

    public void setContinuousDaysDisplay(String continuousDaysDisplay) {
        this.continuousDaysDisplay = continuousDaysDisplay;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    @Override
    public String toString() {
        return "UserStatsDTO{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", original=" + original +
                ", likes=" + likes +
                ", collections=" + collections +
                ", comments=" + comments +
                ", views=" + views +
                ", blogRank=" + blogRank +
                ", followers=" + followers +
                ", following=" + following +
                ", continuousDays=" + continuousDays +
                ", continuousDaysDisplay='" + continuousDaysDisplay + '\'' +
                '}';
    }
}
