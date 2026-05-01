package com.markdown.editor.service;

import com.markdown.editor.entity.Message;
import java.util.List;

public interface MessageService {
    // 发送消息
    void sendMessage(Message message);
    
    // 根据接收者ID查询消息
    List<Message> getMessagesByReceiverId(Long receiverId);
    
    // 根据接收者ID查询消息（分页）
    List<Message> getMessagesByReceiverId(Long receiverId, int page, int pageSize);
    
    // 获取消息总数
    long getMessagesCountByReceiverId(Long receiverId);
    
    // 根据接收者ID和消息类型查询消息
    List<Message> getMessagesByReceiverIdAndType(Long receiverId, String type);
    
    // 根据接收者ID和消息类型查询消息（分页）
    List<Message> getMessagesByReceiverIdAndType(Long receiverId, String type, int page, int pageSize);
    
    // 获取指定类型的消息总数
    long getMessagesCountByReceiverIdAndType(Long receiverId, String type);
    
    // 根据接收者ID和消息类型列表查询消息
    List<Message> getMessagesByReceiverIdAndTypes(Long receiverId, List<String> types);
    
    // 根据接收者ID和消息类型列表查询消息（分页）
    List<Message> getMessagesByReceiverIdAndTypes(Long receiverId, List<String> types, int page, int pageSize);
    
    // 获取指定多个类型的消息总数
    long getMessagesCountByReceiverIdAndTypes(Long receiverId, List<String> types);
    
    // 获取未读消息数量
    int getUnreadMessageCount(Long receiverId);
    
    // 将消息标记为已读
    void markMessagesAsRead(Long receiverId);
    
    // 删除消息
    void deleteMessage(Long messageId);
}
