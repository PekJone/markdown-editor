package com.markdown.editor.service.impl;

import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.DocumentViewStat;
import com.markdown.editor.mapper.DocumentMapper;
import com.markdown.editor.mapper.DocumentViewStatMapper;
import com.markdown.editor.service.DocumentViewStatService;
import com.markdown.editor.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DocumentViewStatServiceImpl implements DocumentViewStatService {

    private static final Logger logger = LogUtils.getLogger(DocumentViewStatServiceImpl.class);

    @Autowired
    private DocumentViewStatMapper documentViewStatMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Override
    @Transactional
    public void recordView(Long documentId, Long userId) {
        try {
            LogUtils.info(logger, "记录文档浏览，文档ID: {}, 用户ID: {}", documentId, userId);

            // 获取文档信息，确认用户ID
            Document document = documentMapper.selectById(documentId);
            if (document == null) {
                LogUtils.warn(logger, "文档不存在，文档ID: {}", documentId);
                return;
            }

            Long authorId = document.getUserId();
            if (authorId == null) {
                LogUtils.warn(logger, "文档没有作者，文档ID: {}", documentId);
                return;
            }
            
            LocalDate today = LocalDate.now();

            // 构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DocumentViewStat> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("document_id", documentId)
                   .eq("user_id", authorId)
                   .eq("view_date", today);

            // 查找当天的记录
            DocumentViewStat existingStat = documentViewStatMapper.selectOne(wrapper);

            if (existingStat != null) {
                // 更新现有记录
                existingStat.setViewCount(existingStat.getViewCount() + 1);
                existingStat.setUpdatedAt(LocalDateTime.now());
                documentViewStatMapper.updateById(existingStat);
                LogUtils.info(logger, "更新浏览量统计，文档ID: {}, 日期: {}, 浏览量: {}", 
                        documentId, today, existingStat.getViewCount());
            } else {
                // 创建新记录
                DocumentViewStat newStat = DocumentViewStat.builder()
                        .documentId(documentId)
                        .userId(authorId)
                        .viewDate(today)
                        .viewCount(1)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                documentViewStatMapper.insert(newStat);
                LogUtils.info(logger, "创建浏览量统计，文档ID: {}, 日期: {}", documentId, today);
            }

            // 同时更新文档的总浏览量
            document.setViewCount(document.getViewCount() + 1);
            documentMapper.updateById(document);
            LogUtils.info(logger, "更新文档总浏览量，文档ID: {}, 总浏览量: {}", documentId, document.getViewCount());

        } catch (Exception e) {
            LogUtils.error(logger, "记录浏览量失败，文档ID: {}", documentId, e);
        }
    }
}
