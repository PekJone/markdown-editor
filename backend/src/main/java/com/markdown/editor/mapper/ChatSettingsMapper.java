package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.ChatSettings;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSettingsMapper extends BaseMapper<ChatSettings> {
}