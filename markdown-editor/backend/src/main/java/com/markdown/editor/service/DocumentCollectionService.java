package com.markdown.editor.service;

import com.markdown.editor.entity.DocumentCollection;

import java.util.List;

public interface DocumentCollectionService {
    DocumentCollection selectById(Long id);
    List<DocumentCollection> selectByUserId(Long userId);
    List<DocumentCollection> selectByDocumentId(Long documentId);
    DocumentCollection selectByUserIdAndDocumentId(Long userId, Long documentId);
    int insert(DocumentCollection documentCollection);
    int deleteById(Long id);
    int deleteByUserIdAndDocumentId(Long userId, Long documentId);
    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);
    int countByDocumentId(Long documentId);
}
