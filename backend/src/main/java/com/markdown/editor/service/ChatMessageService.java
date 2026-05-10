package com.markdown.editor.service;

import com.markdown.editor.dto.ChatUserDTO;
import com.markdown.editor.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {
    // 保存聊天消息
    ChatMessage saveChatMessage(ChatMessage chatMessage);

    // 发送聊天消息
    void sendChatMessage(ChatMessage chatMessage);

    // 获取两个用户之间的聊天记录
    List<ChatMessage> getChatMessages(Long senderId, Long receiverId);

    // 标记聊天消息为已读
    void markChatMessagesAsRead(Long senderId, Long receiverId);

    // 获取用户的聊天列表
    List<ChatUserDTO> getChatList(Long userId);

    // 获取未读消息数量
    int getUnreadChatMessageCount(Long userId);

    // 删除对话（逻辑删除）
    void deleteChatConversation(Long userId, Long partnerId);
}
