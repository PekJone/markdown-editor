package com.markdown.editor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.markdown.editor.entity.Dictionary;
import java.util.List;

public interface DictionaryService extends IService<Dictionary> {
    List<Dictionary> selectByDictType(String dictType);
    Dictionary selectByDictTypeAndCode(String dictType, String dictCode);
    void initDictionary();
}
