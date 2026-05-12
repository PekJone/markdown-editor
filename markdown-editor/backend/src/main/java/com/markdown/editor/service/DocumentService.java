package com.markdown.editor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.entity.Document;

import java.util.List;
import java.util.Map;

public interface DocumentService {
    Document selectById(Long id);
    List<Document> selectByUserId(Long userId);
    List<Document> selectPublicDocuments();
    Page<Document> selectPage(Page<Document> page, Long userId, String category, String tags, String keyword);
    Page<Document> selectPublicPage(Page<Document> page, String category, String tags, String keyword);
    Page<Document> selectAllExceptSecretPage(Page<Document> page, String category, String tags, String keyword);
    Page<Document> selectPageWithCounts(Page<Document> page, Long userId, String category, String tags, String keyword);
    List<Document> selectUserDocumentsWithStats(Long userId);
    List<Document> selectUserCollections(Long userId);
    List<Document> selectDocumentsWithStatsByIds(List<Long> ids);
    long count(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Document> wrapper);
    int insert(Document document);
    int update(Document document);
    int deleteById(Long id);
    int incrementViewCount(Long id);
    int incrementLikeCount(Long id);
    int decrementLikeCount(Long id);
    boolean saveBatch(java.util.List<Document> documents);
    List<Document> selectBatchIds(List<Long> ids);
}
