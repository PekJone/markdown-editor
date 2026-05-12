package com.markdown.editor.service.impl;

import com.markdown.editor.entity.GroupMessage;
import com.markdown.editor.entity.GroupReadStatus;
import com.markdown.editor.entity.User;
import com.markdown.editor.mapper.GroupMessageMapper;
import com.markdown.editor.mapper.GroupReadStatusMapper;
import com.markdown.editor.service.GroupMessageService;
import com.markdown.editor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GroupMessageServiceImpl implements GroupMessageService {

    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private GroupReadStatusMapper groupReadStatusMapper;

    @Autowired
    private UserService userService;

    @Override
    public GroupMessage sendMessage(Long groupId, Long userId, String content) {
        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setUserId(userId);
        message.setContent(content);
        message.setCreatedAt(new Date());
        groupMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<Map<String, Object>> getMessages(Long groupId) {
        List<GroupMessage> messages = groupMessageMapper.selectByGroupId(groupId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (GroupMessage message : messages) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("id", message.getId());
            msgMap.put("groupId", message.getGroupId());
            msgMap.put("senderId", message.getUserId());
            msgMap.put("content", message.getContent());
            msgMap.put("createdAt", message.getCreatedAt());
            
            User user = userService.selectById(message.getUserId());
            if (user != null) {
                msgMap.put("senderAvatar", user.getAvatar());
                msgMap.put("senderNickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
                msgMap.put("senderUsername", user.getUsername());
            }
            
            result.add(msgMap);
        }
        
        return result;
    }

    @Override
    public int getMessageCount(Long groupId) {
        return groupMessageMapper.countByGroupId(groupId);
    }

    @Override
    public int getUnreadMessageCount(Long groupId, Long userId) {
        GroupReadStatus status = groupReadStatusMapper.selectByGroupAndUser(groupId, userId);
        if (status == null || status.getLastReadTime() == null) {
            return groupMessageMapper.countByGroupId(groupId);
        }
        return groupMessageMapper.countUnreadByGroupIdAndTime(groupId, status.getLastReadTime());
    }

    @Override
    public void markMessagesAsRead(Long groupId, Long userId) {
        groupReadStatusMapper.upsertReadStatus(groupId, userId, new Date());
    }
    
    @Override
    public void markMessageAsRead(Long groupId, Long userId, Long messageId) {
        GroupMessage message = groupMessageMapper.selectById(messageId);
        if (message != null && message.getCreatedAt() != null) {
            groupReadStatusMapper.upsertReadStatus(groupId, userId, message.getCreatedAt());
        }
    }
}