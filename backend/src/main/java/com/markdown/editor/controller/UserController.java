package com.markdown.editor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.UserStatsDTO;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.DocumentCollection;
import com.markdown.editor.entity.User;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.DocumentCollectionService;
import com.markdown.editor.service.StatisticsService;
import com.markdown.editor.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.markdown.editor.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Api(tags = "用户管理", description = "用户信息查询、统计、收藏、头像等相关接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentCollectionService documentCollectionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/stats")
    @ApiOperation(value = "获取当前登录用户统计数据", notes = "获取当前登录用户的文档数、粉丝数、关注数等统计信息")
    public ResponseEntity<?> getUserStats(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        UserStatsDTO stats = statisticsService.calculateUserStats(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取用户统计数据成功", stats));
    }

    @GetMapping("/{userId}/stats")
    @ApiOperation(value = "获取指定用户统计数据", notes = "获取指定用户的文档数、粉丝数、关注数等统计信息")
    public ResponseEntity<?> getUserStats(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        LogUtils.info(logger, "开始获取用户统计数据，userId: {}", userId);
        UserStatsDTO stats = statisticsService.calculateUserStats(userId);
        LogUtils.info(logger, "获取用户统计数据成功，stats: {}", stats);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(ApiResponse.success("获取用户统计数据成功", stats));
    }

    @GetMapping("/info")
    @ApiOperation(value = "获取当前登录用户信息", notes = "获取当前登录用户的详细信息")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userService.selectById(userDetails.getId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/collections")
    @ApiOperation(value = "获取用户收藏的文档列表", notes = "获取指定用户收藏的所有文档")
    public ResponseEntity<?> getUserCollections(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        List<Document> collectionDocuments = documentService.selectUserCollections(userId);
        return ResponseEntity.ok(ApiResponse.success("获取收藏列表成功", collectionDocuments));
    }

    @GetMapping("/{userId}")
    @ApiOperation(value = "获取指定用户信息", notes = "根据用户ID获取用户详细信息")
    public ResponseEntity<?> getUserById(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        User user = userService.selectById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @ApiOperation(value = "搜索用户", notes = "根据关键词搜索用户，支持用户名、昵称模糊搜索")
    public ResponseEntity<?> searchUsers(
            @ApiParam(value = "搜索关键词", required = true)
            @RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.success("搜索用户成功", users));
    }

    @PostMapping("/avatar")
    @ApiOperation(value = "更新当前用户头像", notes = "更新当前登录用户的头像")
    public ResponseEntity<?> updateAvatar(
            @ApiParam(value = "头像URL", required = true)
            @RequestParam String avatar, 
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userService.updateAvatar(userDetails.getId(), avatar);
        return ResponseEntity.ok(ApiResponse.success("更新头像成功"));
    }

    @GetMapping("/{userId}/avatar")
    @ApiOperation(value = "获取指定用户头像", notes = "获取指定用户的头像URL")
    public ResponseEntity<?> getAvatar(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        String avatar = userService.getAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success("获取头像成功", avatar));
    }
}
