package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    // 根据接收者ID和消息类型查询消息
    List<Message> selectByReceiverIdAndType(@Param("receiverId") Long receiverId, @Param("type") String type);
    
    // 根据接收者ID查询未读消息数量
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    // 将消息标记为已读
    int updateToReadByReceiverId(@Param("receiverId") Long receiverId);
}
