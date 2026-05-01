package com.markdown.editor.controller;

import com.markdown.editor.entity.Tag;
import com.markdown.editor.service.TagService;
import com.markdown.editor.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Api(tags = "标签管理", description = "文章标签的增删改查接口")
public class TagController {

    @Autowired
    private TagService tagService;

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TagController.class);

    // 获取用户某个分类下的标签列表
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTagsByCategory(
            @PathVariable String category,
            Authentication authentication) {
        
        com.markdown.editor.security.UserDetailsImpl userDetails = (com.markdown.editor.security.UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "用户[{}]获取分类[{}]的标签列表", userDetails.getUsername(), category);
        
        try {
            List<Tag> tags = tagService.selectByUserIdAndCategory(userId, category);
            LogUtils.info(logger, "用户[{}]获取分类[{}]的标签列表成功，找到{}个标签", 
                    userDetails.getUsername(), category, tags.size());
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取分类[{}]的标签列表失败", e, userDetails.getUsername(), category);
            throw e;
        }
    }

    // 添加新标签
    @PostMapping
    public ResponseEntity<?> addTag(
            @RequestBody Tag tag,
            Authentication authentication) {
        
        com.markdown.editor.security.UserDetailsImpl userDetails = (com.markdown.editor.security.UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "用户[{}]添加标签[{}]到分类[{}]", 
                userDetails.getUsername(), tag.getTagName(), tag.getCategory());
        
        try {
            // 检查标签数量限制（每个用户每个分类最多20个标签）
            long tagCount = tagService.countByUserIdAndCategory(userId, tag.getCategory());
            if (tagCount >= 20) {
                LogUtils.warn(logger, "用户[{}]添加标签失败，分类[{}]的标签数量已达上限", 
                        userDetails.getUsername(), tag.getCategory());
                return ResponseEntity.badRequest().body("每个分类最多只能创建20个标签");
            }
            
            // 检查标签是否已存在
            if (tagService.existsByUserIdAndCategoryAndTagName(userId, tag.getCategory(), tag.getTagName())) {
                LogUtils.warn(logger, "用户[{}]添加标签失败，标签[{}]已存在", 
                        userDetails.getUsername(), tag.getTagName());
                return ResponseEntity.badRequest().body("该标签已存在");
            }
            
            // 设置用户ID和时间戳
            tag.setUserId(userId);
            tag.setCreatedAt(new Date());
            tag.setUpdatedAt(new Date());
            
            int result = tagService.insert(tag);
            if (result > 0) {
                LogUtils.info(logger, "用户[{}]添加标签[{}]成功", userDetails.getUsername(), tag.getTagName());
                return ResponseEntity.ok(tag);
            } else {
                LogUtils.error(logger, "用户[{}]添加标签[{}]失败", userDetails.getUsername(), tag.getTagName());
                return ResponseEntity.badRequest().body("添加标签失败");
            }
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]添加标签失败", e, userDetails.getUsername());
            throw e;
        }
    }

    // 删除标签
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(
            @PathVariable Long id,
            Authentication authentication) {
        
        com.markdown.editor.security.UserDetailsImpl userDetails = (com.markdown.editor.security.UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "用户[{}]删除标签ID: {}", userDetails.getUsername(), id);
        
        try {
            // 检查标签是否存在且属于当前用户
            Tag tag = tagService.selectById(id);
            if (tag == null) {
                LogUtils.warn(logger, "用户[{}]删除标签失败，标签不存在", userDetails.getUsername());
                return ResponseEntity.notFound().build();
            }
            
            if (!tag.getUserId().equals(userId)) {
                LogUtils.warn(logger, "用户[{}]删除标签失败，无权限操作", userDetails.getUsername());
                return ResponseEntity.status(403).body("无权限操作该标签");
            }
            
            int result = tagService.deleteById(id);
            if (result > 0) {
                LogUtils.info(logger, "用户[{}]删除标签ID: {}成功", userDetails.getUsername(), id);
                return ResponseEntity.ok("删除标签成功");
            } else {
                LogUtils.error(logger, "用户[{}]删除标签ID: {}失败", userDetails.getUsername(), id);
                return ResponseEntity.badRequest().body("删除标签失败");
            }
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]删除标签失败", e, userDetails.getUsername());
            throw e;
        }
    }

    // 获取用户的所有标签
    @GetMapping
    public ResponseEntity<?> getAllTags(Authentication authentication) {
        com.markdown.editor.security.UserDetailsImpl userDetails = (com.markdown.editor.security.UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "用户[{}]获取所有标签", userDetails.getUsername());
        
        try {
            List<Tag> tags = tagService.selectByUserId(userId);
            LogUtils.info(logger, "用户[{}]获取所有标签成功，找到{}个标签", userDetails.getUsername(), tags.size());
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取所有标签失败", e, userDetails.getUsername());
            throw e;
        }
    }
}