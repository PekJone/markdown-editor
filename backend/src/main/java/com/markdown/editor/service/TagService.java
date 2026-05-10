package com.markdown.editor.service;

import com.markdown.editor.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> selectByUserIdAndCategory(Long userId, String category);
    Tag selectById(Long id);
    int insert(Tag tag);
    int update(Tag tag);
    int deleteById(Long id);
    long countByUserIdAndCategory(Long userId, String category);
    boolean existsByUserIdAndCategoryAndTagName(Long userId, String category, String tagName);
    List<Tag> selectByUserId(Long userId);
}