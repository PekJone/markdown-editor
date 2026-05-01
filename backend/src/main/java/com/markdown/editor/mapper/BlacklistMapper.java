package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {
}