package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.entity.Document;
import com.markdown.editor.mapper.DocumentMapper;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.UserStatisticsService;
import com.markdown.editor.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LogUtils.getLogger(DocumentServiceImpl.class);

    @Resource
    private DocumentMapper documentMapper;

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Override
    public Document selectById(Long id) {
        return documentMapper.selectById(id);
    }

    @Override
    public List<Document> selectByUserId(Long userId) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("updated_at");
        return documentMapper.selectList(wrapper);
    }

    @Override
    public List<Document> selectPublicDocuments() {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.eq("permission", "public");
        wrapper.orderByDesc("updated_at");
        return documentMapper.selectList(wrapper);
    }

    @Override
    public Page<Document> selectPage(Page<Document> page, Long userId, String category, String tags, String keyword) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        if (tags != null && !tags.isEmpty()) {
            wrapper.like("tags", tags);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(i -> i.like("title", keyword).or().like("content", keyword));
        }
        wrapper.orderByDesc("updated_at");
        return documentMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Document> selectPublicPage(Page<Document> page, String category, String tags, String keyword) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.eq("permission", "public");
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        if (tags != null && !tags.isEmpty()) {
            wrapper.like("tags", tags);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(i -> i.like("title", keyword).or().like("content", keyword));
        }
        wrapper.orderByDesc("updated_at");
        
        LogUtils.info(logger, "分页参数：current={}, size={}, offset={}", page.getCurrent(), page.getSize(), page.offset());
        
        return documentMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Document> selectAllExceptSecretPage(Page<Document> page, String category, String tags, String keyword) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.ne("permission", "secret");
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        if (tags != null && !tags.isEmpty()) {
            wrapper.like("tags", tags);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(i -> i.like("title", keyword).or().like("content", keyword));
        }
        wrapper.orderByDesc("updated_at");
        return documentMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(Document document) {
        int result = documentMapper.insert(document);
        if (result > 0 && document.getUserId() != null) {
            userStatisticsService.incrementBlogCount(document.getUserId());
        }
        return result;
    }

    @Override
    public int update(Document document) {
        return documentMapper.updateById(document);
    }

    @Override
    public int deleteById(Long id) {
        Document document = documentMapper.selectById(id);
        int result = documentMapper.deleteById(id);
        if (result > 0 && document != null && document.getUserId() != null) {
            userStatisticsService.decrementBlogCount(document.getUserId());
        }
        return result;
    }

    private static final java.util.Map<Long, Long> viewCache = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public int incrementViewCount(Long id) {
        Long lastViewTime = viewCache.get(id);
        long now = System.currentTimeMillis();
        
        if (lastViewTime != null && now - lastViewTime < 1000) {
            return 0;
        }
        
        viewCache.put(id, now);
        
        Document document = documentMapper.selectById(id);
        if (document != null) {
            Integer viewCount = document.getViewCount();
            if (viewCount == null) {
                viewCount = 0;
            }
            document.setViewCount(viewCount + 1);
            return documentMapper.updateById(document);
        }
        return 0;
    }

    @Override
    public int incrementLikeCount(Long id) {
        Document document = documentMapper.selectById(id);
        if (document != null) {
            Integer likeCount = document.getLikeCount();
            if (likeCount == null) {
                likeCount = 0;
            }
            document.setLikeCount(likeCount + 1);
            int result = documentMapper.updateById(document);
            if (result > 0 && document.getUserId() != null) {
                userStatisticsService.incrementLikesCount(document.getUserId());
            }
            return result;
        }
        return 0;
    }

    @Override
    public int decrementLikeCount(Long id) {
        Document document = documentMapper.selectById(id);
        if (document != null) {
            Integer likeCount = document.getLikeCount();
            if (likeCount != null && likeCount > 0) {
                document.setLikeCount(likeCount - 1);
                int result = documentMapper.updateById(document);
                if (result > 0 && document.getUserId() != null) {
                    userStatisticsService.decrementLikesCount(document.getUserId());
                }
                return result;
            }
        }
        return 0;
    }

    @Override
    public long count(QueryWrapper<Document> wrapper) {
        return documentMapper.selectCount(wrapper);
    }

    @Override
    public boolean saveBatch(List<Document> documents) {
        for (Document document : documents) {
            documentMapper.insert(document);
        }
        return true;
    }

    @Override
    public List<Document> selectBatchIds(List<Long> ids) {
        return documentMapper.selectBatchIds(ids);
    }
}
