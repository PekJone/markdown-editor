package com.markdown.editor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.markdown.editor.dto.CommentDTO;
import com.markdown.editor.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> selectByDocumentId(Long documentId);

    Comment selectById(Long id);

    int insert(Comment comment);

    int deleteById(Long id);

    int countByDocumentId(Long documentId);

    List<Comment> selectByUserId(Long userId);

    List<Comment> selectByDocumentIds(List<Long> documentIds);

    List<CommentDTO> getMyCommentsWithDetails(Long userId, int page, int size);

    List<CommentDTO> getReceivedCommentsWithDetails(Long userId, int page, int size);
}
