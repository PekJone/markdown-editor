package com.markdown.editor.service;

import com.markdown.editor.entity.GroupChat;
import java.util.List;
import java.util.Map;

public interface GroupChatService {
    GroupChat createGroup(Long creatorId, String name, String description, List<Long> memberIds);
    GroupChat getGroupById(Long groupId);
    List<GroupChat> getGroupsByUserId(Long userId);
    void addMember(Long groupId, Long userId);
    void removeMember(Long groupId, Long userId, Long operatorId);
    void updateGroup(Long groupId, String name, String description);
    void updateAnnouncement(Long groupId, String announcement, Long publisherId);
    void deleteGroup(Long groupId, Long userId);
    List<Map<String, Object>> getGroupMembers(Long groupId);
    void setAdmin(Long groupId, Long userId, Long operatorId);
    void removeAdmin(Long groupId, Long userId, Long operatorId);
    int getAdminCount(Long groupId);
}