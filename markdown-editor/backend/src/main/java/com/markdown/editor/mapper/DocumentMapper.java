package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.Document;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DocumentMapper extends BaseMapper<Document> {
    
    List<Document> selectPageWithCounts(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("tags") String tags,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("size") Integer size);
    
    Long selectPageWithCountsCount(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("tags") String tags,
            @Param("keyword") String keyword);
    
    List<Document> selectUserDocumentsWithStats(@Param("userId") Long userId);
    
    List<Document> selectUserCollections(@Param("userId") Long userId);
    
    List<Document> selectDocumentsWithStatsByIds(@Param("ids") List<Long> ids);
    
    List<Document> selectAllExceptSecretWithCounts(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("size") Integer size);
    
    List<Document> selectPublicWithCounts(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("size") Integer size);
}
