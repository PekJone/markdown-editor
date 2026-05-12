package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.DocumentCollection;
import com.markdown.editor.mapper.DocumentCollectionMapper;
import com.markdown.editor.service.DocumentCollectionService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.UserStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class DocumentCollectionServiceImpl implements DocumentCollectionService {

    @Resource
    private DocumentCollectionMapper documentCollectionMapper;

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Autowired
    private DocumentService documentService;

    @Override
    public DocumentCollection selectById(Long id) {
        return documentCollectionMapper.selectById(id);
    }

    @Override
    public List<DocumentCollection> selectByUserId(Long userId) {
        QueryWrapper<DocumentCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        return documentCollectionMapper.selectList(wrapper);
    }

    @Override
    public List<DocumentCollection> selectByDocumentId(Long documentId) {
        QueryWrapper<DocumentCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        return documentCollectionMapper.selectList(wrapper);
    }

    @Override
    public DocumentCollection selectByUserIdAndDocumentId(Long userId, Long documentId) {
        QueryWrapper<DocumentCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("document_id", documentId);
        return documentCollectionMapper.selectOne(wrapper);
    }

    @Override
    public int insert(DocumentCollection documentCollection) {
        // 设置创建时间
        documentCollection.setCreatedAt(new Date());
        int result = documentCollectionMapper.insert(documentCollection);
        if (result > 0 && documentCollection.getDocumentId() != null) {
            Document document = documentService.selectById(documentCollection.getDocumentId());
            if (document != null) {
                // 更新文档的收藏数
                document.setCollectionCount(document.getCollectionCount() + 1);
                documentService.update(document);
                
                // 更新用户的收藏统计数
                if (document.getUserId() != null) {
                    userStatisticsService.incrementCollectionsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public int deleteById(Long id) {
        DocumentCollection collection = documentCollectionMapper.selectById(id);
        int result = documentCollectionMapper.deleteById(id);
        if (result > 0 && collection != null && collection.getDocumentId() != null) {
            Document document = documentService.selectById(collection.getDocumentId());
            if (document != null) {
                // 更新文档的收藏数
                if (document.getCollectionCount() > 0) {
                    document.setCollectionCount(document.getCollectionCount() - 1);
                    documentService.update(document);
                }
                
                // 更新用户的收藏统计数
                if (document.getUserId() != null) {
                    userStatisticsService.decrementCollectionsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public int deleteByUserIdAndDocumentId(Long userId, Long documentId) {
        QueryWrapper<DocumentCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("document_id", documentId);
        int result = documentCollectionMapper.delete(wrapper);
        if (result > 0 && documentId != null) {
            Document document = documentService.selectById(documentId);
            if (document != null) {
                // 更新文档的收藏数
                if (document.getCollectionCount() > 0) {
                    document.setCollectionCount(document.getCollectionCount() - 1);
                    documentService.update(document);
                }
                
                // 更新用户的收藏统计数
                if (document.getUserId() != null) {
                    userStatisticsService.decrementCollectionsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public boolean existsByUserIdAndDocumentId(Long userId, Long documentId) {
        QueryWrapper<DocumentCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("document_id", documentId);
        return documentCollectionMapper.exists(wrapper);
    }

    @Override
    public int countByDocumentId(Long documentId) {
        QueryWrapper<DocumentCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        Long count = documentCollectionMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }
}
