package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markdown.editor.dto.ChatUserDTO;
import com.markdown.editor.entity.ChatMessage;
import com.markdown.editor.mapper.ChatMessageMapper;
import com.markdown.editor.service.BlacklistService;
import com.markdown.editor.service.ChatMessageService;
import com.markdown.editor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BlacklistService blacklistService;

    @Override
    public ChatMessage saveChatMessage(ChatMessage chatMessage) {
        chatMessage.setIsRead(false);
        chatMessage.setDeleted(false);
        chatMessage.setCreatedAt(new Date());
        chatMessage.setUpdatedAt(new Date());
        chatMessageMapper.insert(chatMessage);
        return chatMessage;
    }

    @Override
    public void sendChatMessage(ChatMessage chatMessage) {
        try {
            chatMessage.setIsRead(false);
            chatMessage.setDeleted(false);
            chatMessage.setCreatedAt(new Date());
            chatMessage.setUpdatedAt(new Date());
            chatMessageMapper.insert(chatMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ChatMessage> getChatMessages(Long senderId, Long receiverId) {
        try {
            // 使用新的查询方法，排除已删除的消息
            return chatMessageMapper.selectChatMessagesBetweenUsers(senderId, receiverId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteChatConversation(Long userId, Long partnerId) {
        try {
            // 直接执行物理删除，确保聊天记录真正被删除
            chatMessageMapper.physicalDeleteByUsers(userId, partnerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markChatMessagesAsRead(Long senderId, Long receiverId) {
        try {
            chatMessageMapper.updateToReadBySenderIdAndReceiverId(senderId, receiverId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ChatUserDTO> getChatList(Long userId) {
        try {
            // 获取聊天消息列表
            List<ChatMessage> chatMessages = chatMessageMapper.selectChatListByUserId(userId);

            // 转换为聊天用户列表
            List<ChatUserDTO> chatUserDTOs = chatMessages.stream().filter(chatMessage -> {
                // 过滤掉 senderId 或 receiverId 为 null 的消息
                if (chatMessage.getSenderId() == null || chatMessage.getReceiverId() == null) {
                    return false;
                }
                // 确定对方用户ID
                Long otherUserId = chatMessage.getSenderId().equals(userId) ? chatMessage.getReceiverId() : chatMessage.getSenderId();
                // 过滤掉在黑名单中的用户（但仍然显示他们的消息）
                return true;
            }).map(chatMessage -> {
                ChatUserDTO chatUserDTO = new ChatUserDTO();

                // 确定对方用户ID
                Long otherUserId = chatMessage.getSenderId().equals(userId) ? chatMessage.getReceiverId() : chatMessage.getSenderId();

                // 获取对方用户信息
                com.markdown.editor.entity.User otherUser = userService.selectById(otherUserId);
                if (otherUser != null) {
                    chatUserDTO.setId(otherUser.getId());
                    chatUserDTO.setUsername(otherUser.getUsername());
                    chatUserDTO.setNickname(otherUser.getNickname());
                    chatUserDTO.setAvatar(otherUser.getAvatar());
                }

                // 设置最后一条消息和时间
                chatUserDTO.setLastMessage(chatMessage.getContent());
                chatUserDTO.setLastMessageTime(chatMessage.getCreatedAt());

                // 获取未读消息数量（排除黑名单用户的消息）
                QueryWrapper<ChatMessage> unreadWrapper = new QueryWrapper<>();
                unreadWrapper.eq("sender_id", otherUserId)
                            .eq("receiver_id", userId)
                            .eq("is_read", false);
                int unreadCount = chatMessageMapper.selectCount(unreadWrapper).intValue();
                chatUserDTO.setUnreadCount(unreadCount);

                return chatUserDTO;
            }).collect(Collectors.toList());

            // 去重，确保每个用户只出现一次
            Map<Long, ChatUserDTO> uniqueUsers = chatUserDTOs.stream()
                .collect(Collectors.toMap(ChatUserDTO::getId, user -> user, (existing, replacement) -> existing));

            return new ArrayList<>(uniqueUsers.values());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public int getUnreadChatMessageCount(Long userId) {
        try {
            // 获取所有未读消息
            QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
            wrapper.eq("receiver_id", userId).eq("is_read", false);
            List<ChatMessage> unreadMessages = chatMessageMapper.selectList(wrapper);
            
            // 过滤掉黑名单用户的消息
            int count = 0;
            for (ChatMessage message : unreadMessages) {
                if (!blacklistService.isBlacklisted(userId, message.getSenderId())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
