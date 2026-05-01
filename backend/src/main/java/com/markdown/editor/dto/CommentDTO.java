package com.markdown.editor.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private Long documentId;
    private Long userId;
    private Long parentId; // 父评论ID，用于回复功能
    private Long repliedUserId; // 被回复用户ID
    private String username; // 评论者用户名
    private String nickname; // 评论者昵称
    private String repliedUsername; // 被回复用户的用户名
    private String repliedNickname; // 被回复用户的昵称
    private String content;
    private LocalDateTime createdAt;
    private String documentTitle;

    public CommentDTO() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getRepliedUserId() {
        return repliedUserId;
    }

    public void setRepliedUserId(Long repliedUserId) {
        this.repliedUserId = repliedUserId;
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

    public String getRepliedUsername() {
        return repliedUsername;
    }

    public void setRepliedUsername(String repliedUsername) {
        this.repliedUsername = repliedUsername;
    }

    public String getRepliedNickname() {
        return repliedNickname;
    }

    public void setRepliedNickname(String repliedNickname) {
        this.repliedNickname = repliedNickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }
}