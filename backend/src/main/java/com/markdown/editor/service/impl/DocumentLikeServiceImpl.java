package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markdown.editor.entity.DocumentLike;
import com.markdown.editor.mapper.DocumentLikeMapper;
import com.markdown.editor.service.DocumentLikeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DocumentLikeServiceImpl implements DocumentLikeService {

    @Resource
    private DocumentLikeMapper documentLikeMapper;

    @Override
    public DocumentLike selectById(Long id) {
        return documentLikeMapper.selectById(id);
    }

    @Override
    public List<DocumentLike> selectByUserId(Long userId) {
        QueryWrapper<DocumentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        return documentLikeMapper.selectList(wrapper);
    }

    @Override
    public List<DocumentLike> selectByDocumentId(Long documentId) {
        QueryWrapper<DocumentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        return documentLikeMapper.selectList(wrapper);
    }

    @Override
    public DocumentLike selectByUserIdAndDocumentId(Long userId, Long documentId) {
        QueryWrapper<DocumentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("document_id", documentId);
        return documentLikeMapper.selectOne(wrapper);
    }

    @Override
    public int insert(DocumentLike documentLike) {
        return documentLikeMapper.insert(documentLike);
    }

    @Override
    public int deleteById(Long id) {
        return documentLikeMapper.deleteById(id);
    }

    @Override
    public int deleteByUserIdAndDocumentId(Long userId, Long documentId) {
        QueryWrapper<DocumentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("document_id", documentId);
        return documentLikeMapper.delete(wrapper);
    }

    @Override
    public boolean existsByUserIdAndDocumentId(Long userId, Long documentId) {
        QueryWrapper<DocumentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("document_id", documentId);
        return documentLikeMapper.exists(wrapper);
    }
}
