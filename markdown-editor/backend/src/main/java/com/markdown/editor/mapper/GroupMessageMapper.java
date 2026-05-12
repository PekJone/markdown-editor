package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.GroupMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {
    List<GroupMessage> selectByGroupId(@Param("groupId") Long groupId);
    
    int countByGroupId(@Param("groupId") Long groupId);
    
    int countUnreadByGroupIdAndTime(@Param("groupId") Long groupId, @Param("lastReadTime") java.util.Date lastReadTime);
}