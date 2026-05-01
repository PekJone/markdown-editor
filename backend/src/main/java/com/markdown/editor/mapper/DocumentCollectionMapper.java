package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.DocumentCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Mapper
public interface DocumentCollectionMapper extends BaseMapper<DocumentCollection> {
    
    List<Map<String, Object>> countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startTime") Timestamp startTime,
            @Param("endTime") Timestamp endTime
    );
}
