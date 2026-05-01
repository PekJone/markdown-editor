package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.entity.Comment;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.Message;
import com.markdown.editor.mapper.CommentMapper;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.MessageService;
import com.markdown.editor.service.UserStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MessageService messageService;

    @Override
    public List<Comment> selectByDocumentId(Long documentId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Comment selectById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int insert(Comment comment) {
        int result = baseMapper.insert(comment);
        if (result > 0 && comment.getDocumentId() != null) {
            Document document = documentService.selectById(comment.getDocumentId());
            if (document != null) {
                // 更新文档的评论数
                document.setCommentCount(document.getCommentCount() + 1);
                documentService.update(document);
                
                // 更新用户的评论统计数
                if (document.getUserId() != null) {
                    userStatisticsService.incrementCommentsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public int deleteById(Long id) {
        Comment comment = baseMapper.selectById(id);
        int result = baseMapper.deleteById(id);
        if (result > 0 && comment != null && comment.getDocumentId() != null) {
            Document document = documentService.selectById(comment.getDocumentId());
            if (document != null) {
                // 更新文档的评论数
                if (document.getCommentCount() > 0) {
                    document.setCommentCount(document.getCommentCount() - 1);
                    documentService.update(document);
                }
                
                // 更新用户的评论统计数
                if (document.getUserId() != null) {
                    userStatisticsService.decrementCommentsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public int countByDocumentId(Long documentId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        return baseMapper.selectCount(wrapper).intValue();
    }

    @Override
    public List<Comment> selectByUserId(Long userId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Comment> selectByDocumentIds(List<Long> documentIds) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("document_id", documentIds);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }
}