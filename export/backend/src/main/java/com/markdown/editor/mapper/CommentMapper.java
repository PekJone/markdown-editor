package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.dto.CommentDTO;
import com.markdown.editor.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    List<Map<String, Object>> countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startTime") Timestamp startTime,
            @Param("endTime") Timestamp endTime
    );

    List<CommentDTO> selectMyCommentsWithDetails(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    Long countMyComments(@Param("userId") Long userId);

    List<CommentDTO> selectReceivedCommentsWithDetails(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    Long countReceivedComments(@Param("userId") Long userId);
}
