package com.markdown.editor.service;

import com.markdown.editor.entity.GroupMessage;
import java.util.List;
import java.util.Map;

public interface GroupMessageService {
    GroupMessage sendMessage(Long groupId, Long userId, String content);
    
    List<Map<String, Object>> getMessages(Long groupId);
    
    int getMessageCount(Long groupId);
    
    int getUnreadMessageCount(Long groupId, Long userId);
    
    void markMessagesAsRead(Long groupId, Long userId);
    
    void markMessageAsRead(Long groupId, Long userId, Long messageId);
}