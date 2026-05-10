package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.entity.Dictionary;
import com.markdown.editor.mapper.DictionaryMapper;
import com.markdown.editor.service.DictionaryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {

    @Override
    public List<Dictionary> selectByDictType(String dictType) {
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType);
        wrapper.eq("status", "1");
        wrapper.orderByAsc("id");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Dictionary selectByDictTypeAndCode(String dictType, String dictCode) {
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType);
        wrapper.eq("dict_code", dictCode);
        wrapper.eq("status", "1");
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void initDictionary() {
        // 初始化文章分类字典
        initCategoryDictionary();
        // 初始化文章权限字典
        initPermissionDictionary();
        // 初始化角色字典
        initRoleDictionary();
    }

    private void initCategoryDictionary() {
        // 数据已通过数据库初始化脚本插入，此处仅作为备份
        String dictType = "article_category";
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType);
        long count = baseMapper.selectCount(wrapper);
        if (count == 0) {
            // 如果数据库中没有数据，才进行初始化
            System.out.println("字典表中文章分类数据不存在，开始初始化...");
        }
    }

    private void initPermissionDictionary() {
        // 数据已通过数据库初始化脚本插入，此处仅作为备份
        String dictType = "article_permission";
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType);
        long count = baseMapper.selectCount(wrapper);
        if (count == 0) {
            // 如果数据库中没有数据，才进行初始化
            System.out.println("字典表中文章权限数据不存在，开始初始化...");
        }
    }

    private void initRoleDictionary() {
        // 数据已通过数据库初始化脚本插入，此处仅作为备份
        String dictType = "user_role";
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType);
        long count = baseMapper.selectCount(wrapper);
        if (count == 0) {
            // 如果数据库中没有数据，才进行初始化
            System.out.println("字典表中角色数据不存在，开始初始化...");
        }
    }
}
