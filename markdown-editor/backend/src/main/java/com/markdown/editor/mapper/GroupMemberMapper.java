package com.markdown.editor.mapper;

import com.markdown.editor.entity.GroupMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    List<GroupMember> selectByGroupId(@Param("groupId") Long groupId);
    List<GroupMember> selectByUserId(@Param("userId") Long userId);
    GroupMember selectByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}