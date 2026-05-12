package com.markdown.editor.controller;

import com.markdown.editor.service.DictionaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary")
@Api(tags = "数据字典", description = "文章分类、权限等字典查询的接口")
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping("/type/{dictType}")
    public ResponseEntity<?> getDictionaryByType(@PathVariable String dictType) {
        return ResponseEntity.ok(dictionaryService.selectByDictType(dictType));
    }
}
