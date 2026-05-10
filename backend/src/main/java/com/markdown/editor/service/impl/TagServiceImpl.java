package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.entity.Tag;
import com.markdown.editor.mapper.TagMapper;
import com.markdown.editor.service.TagService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {


    @Override
    public List<Tag> selectByUserIdAndCategory(Long userId, String category) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("category", category);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Tag selectById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int insert(Tag tag) {
        return baseMapper.insert(tag);
    }

    @Override
    public int update(Tag tag) {
        return baseMapper.updateById(tag);
    }

    @Override
    public int deleteById(Long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public long countByUserIdAndCategory(Long userId, String category) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("category", category);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public boolean existsByUserIdAndCategoryAndTagName(Long userId, String category, String tagName) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("category", category);
        wrapper.eq("tag_name", tagName);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Tag> selectByUserId(Long userId) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }
}