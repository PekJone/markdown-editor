package com.markdown.editor.service;

import com.markdown.editor.entity.DocumentLike;

import java.util.List;

public interface DocumentLikeService {
    DocumentLike selectById(Long id);
    List<DocumentLike> selectByUserId(Long userId);
    List<DocumentLike> selectByDocumentId(Long documentId);
    DocumentLike selectByUserIdAndDocumentId(Long userId, Long documentId);
    int insert(DocumentLike documentLike);
    int deleteById(Long id);
    int deleteByUserIdAndDocumentId(Long userId, Long documentId);
    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);
}
