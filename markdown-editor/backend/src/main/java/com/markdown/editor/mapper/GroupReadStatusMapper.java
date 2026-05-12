package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.GroupReadStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GroupReadStatusMapper extends BaseMapper<GroupReadStatus> {
    
    @Select("SELECT * FROM group_read_status WHERE group_id = #{groupId} AND user_id = #{userId}")
    GroupReadStatus selectByGroupAndUser(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    @Update("INSERT INTO group_read_status (group_id, user_id, last_read_time) " +
            "VALUES (#{groupId}, #{userId}, #{lastReadTime}) " +
            "ON DUPLICATE KEY UPDATE last_read_time = #{lastReadTime}")
    void upsertReadStatus(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("lastReadTime") java.util.Date lastReadTime);
}