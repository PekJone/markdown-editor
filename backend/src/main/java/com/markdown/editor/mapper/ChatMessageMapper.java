package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    // 根据发送者ID和接收者ID更新消息为已读
    int updateToReadBySenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // 根据用户ID获取聊天列表
    List<ChatMessage> selectChatListByUserId(@Param("userId") Long userId);

    // 根据发送者ID和接收者ID逻辑删除消息
    int logicalDeleteByUsers(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    // 检查双方是否都已删除对话
    int checkBothUsersDeleted(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    // 物理删除双方都已删除的消息
    int physicalDeleteByUsers(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    // 获取两个用户之间的聊天记录（排除已删除的）
    List<ChatMessage> selectChatMessagesBetweenUsers(@Param("userId") Long userId, @Param("partnerId") Long partnerId);
}
