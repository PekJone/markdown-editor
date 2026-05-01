package com.markdown.editor.controller;

import com.markdown.editor.constant.SystemConstant;
import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.CommentDTO;
import com.markdown.editor.entity.Comment;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.Message;
import com.markdown.editor.entity.User;
import com.markdown.editor.mapper.UserMapper;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.MessageService;
import com.markdown.editor.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
@Api(tags = "评论管理", description = "评论的添加、删除、查询等接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private DocumentService documentService;

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CommentController.class);



    @GetMapping("/document/{documentId}")
    public ResponseEntity<?> getCommentsByDocumentId(@PathVariable Long documentId) {
        LogUtils.info(logger, "获取文档[{}]的评论列表", documentId);

        try {
            List<Comment> comments = commentService.selectByDocumentId(documentId);
            List<CommentDTO> commentDTOs = new ArrayList<>();

            for (Comment comment : comments) {
                CommentDTO dto = convertToDTO(comment);
                commentDTOs.add(dto);
            }

            LogUtils.info(logger, "获取文档[{}]的评论列表成功，共{}条评论", documentId, commentDTOs.size());
            return ResponseEntity.ok(commentDTOs);
        } catch (Exception e) {
            LogUtils.error(logger, "获取文档[{}]的评论列表失败", e, documentId);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Comment comment, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]添加评论，文档ID: {}, 父评论ID: {}", userDetails.getUsername(), comment.getDocumentId(), comment.getParentId());

        try {
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("评论内容不能为空");
            }

            if (comment.getContent().length() > SystemConstant.MAX_COMMENT_LENGTH) {
                return ResponseEntity.badRequest().body("评论内容不能超过" + SystemConstant.MAX_COMMENT_LENGTH + "个字符");
            }

            comment.setUserId(userDetails.getId());
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());

            // 如果是回复评论，设置父评论ID
            if (comment.getParentId() != null) {
                LogUtils.info(logger, "用户[{}]回复评论，父评论ID: {}", userDetails.getUsername(), comment.getParentId());
            }

            int result = commentService.insert(comment);
            if (result > 0) {
                LogUtils.info(logger, "用户[{}]添加评论成功，评论ID: {}", userDetails.getUsername(), comment.getId());
                
                // 发送评论消息通知
                Document document = documentService.selectById(comment.getDocumentId());
                if (document != null) {
                    // 通知文章作者（排除自己评论自己文章的情况）
                    if (!document.getUserId().equals(userDetails.getId())) {
                        Message message = new Message();
                        message.setSenderId(userDetails.getId());
                        message.setReceiverId(document.getUserId());
                        message.setType("comment");
                        message.setContent("您的文章《" + document.getTitle() + "》收到了新评论");
                        message.setRelatedId(comment.getId());
                        messageService.sendMessage(message);
                    }
                    
                    // 如果是回复评论，通知被回复的用户（排除回复自己的情况）
                    if (comment.getParentId() != null) {
                        try {
                            Comment parentComment = commentService.selectById(comment.getParentId());
                            if (parentComment != null && !parentComment.getUserId().equals(userDetails.getId()) && !parentComment.getUserId().equals(document.getUserId())) {
                                Message message = new Message();
                                message.setSenderId(userDetails.getId());
                                message.setReceiverId(parentComment.getUserId());
                                message.setType("comment_replay");
                                message.setContent("您的评论收到了新回复");
                                message.setRelatedId(comment.getId());
                                messageService.sendMessage(message);
                            }
                        } catch (Exception e) {
                            // 忽略获取父评论时的异常
                        }
                    }
                }
                
                CommentDTO dto = convertToDTO(comment);
                return ResponseEntity.ok(dto);
            } else {
                LogUtils.error(logger, "用户[{}]添加评论失败", userDetails.getUsername());
                return ResponseEntity.badRequest().body("添加评论失败");
            }
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]添加评论失败", e, userDetails.getUsername());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]删除评论，评论ID: {}", userDetails.getUsername(), id);

        try {
            Comment comment = commentService.selectById(id);
            if (comment == null) {
                LogUtils.warn(logger, "用户[{}]删除评论失败，评论不存在，评论ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }

            // 检查是否是管理员（通过角色字段）、评论的创建者或文章的作者
            boolean isAdmin = SystemConstant.ROLE_ADMIN.equals(userDetails.getRole());
            
            // 获取评论所属的文章
            Document document = documentService.selectById(comment.getDocumentId());
            boolean isDocumentAuthor = document != null && document.getUserId().equals(userDetails.getId());

            if (!comment.getUserId().equals(userDetails.getId()) && !isAdmin && !isDocumentAuthor) {
                LogUtils.warn(logger, "用户[{}]删除评论失败，无权限，评论ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.status(403).body("无权限删除该评论");
            }

            int result = commentService.deleteById(id);
            if (result > 0) {
                LogUtils.info(logger, "用户[{}]删除评论成功，评论ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.ok("删除成功");
            } else {
                LogUtils.error(logger, "用户[{}]删除评论失败，评论ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.badRequest().body("删除失败");
            }
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]删除评论失败", e, userDetails.getUsername());
            throw e;
        }
    }

    @GetMapping("/count/{documentId}")
    public ResponseEntity<?> getCommentCount(@PathVariable Long documentId) {
        int count = commentService.countByDocumentId(documentId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyComments(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取自己的评论列表，页码: {}, 每页大小: {}", userDetails.getUsername(), page, size);

        try {
            List<Comment> comments = commentService.selectByUserId(userDetails.getId());
            List<CommentDTO> commentDTOs = new ArrayList<>();

            for (Comment comment : comments) {
                CommentDTO dto = convertToDTO(comment);
                // 添加文章标题
                Document document = documentService.selectById(comment.getDocumentId());
                if (document != null) {
                    dto.setDocumentTitle(document.getTitle());
                }
                commentDTOs.add(dto);
            }

            // 分页处理
            int total = commentDTOs.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            List<CommentDTO> paginatedComments = new ArrayList<>();
            if (start < total) {
                paginatedComments = new ArrayList<>(commentDTOs.subList(start, end));
            }

            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("records", paginatedComments);
            response.put("total", total);
            response.put("size", size);
            response.put("current", page + 1);

            LogUtils.info(logger, "用户[{}]获取自己的评论列表成功，共{}条评论，当前页{}条", userDetails.getUsername(), total, paginatedComments.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取自己的评论列表失败", e, userDetails.getUsername());
            throw e;
        }
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedComments(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取收到的评论列表，页码: {}, 每页大小: {}", userDetails.getUsername(), page, size);

        try {
            // 获取用户的所有文章
            List<Document> documents = documentService.selectByUserId(userDetails.getId());
            List<Long> documentIds = new ArrayList<>();
            for (Document document : documents) {
                documentIds.add(document.getId());
            }

            // 获取这些文章的评论
            List<Comment> comments = new ArrayList<>();
            if (!documentIds.isEmpty()) {
                comments = commentService.selectByDocumentIds(documentIds);
            }

            List<CommentDTO> commentDTOs = new ArrayList<>();
            for (Comment comment : comments) {
                // 排除自己的评论
                if (!comment.getUserId().equals(userDetails.getId())) {
                    CommentDTO dto = convertToDTO(comment);
                    // 添加文章标题
                    Document document = documentService.selectById(comment.getDocumentId());
                    if (document != null) {
                        dto.setDocumentTitle(document.getTitle());
                    }
                    commentDTOs.add(dto);
                }
            }

            // 分页处理
            int total = commentDTOs.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            List<CommentDTO> paginatedComments = new ArrayList<>();
            if (start < total) {
                paginatedComments = new ArrayList<>(commentDTOs.subList(start, end));
            }

            // 构建分页响应
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("records", paginatedComments);
            response.put("total", total);
            response.put("size", size);
            response.put("current", page + 1);

            LogUtils.info(logger, "用户[{}]获取收到的评论列表成功，共{}条评论，当前页{}条", userDetails.getUsername(), total, paginatedComments.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取收到的评论列表失败", e, userDetails.getUsername());
            throw e;
        }
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setDocumentId(comment.getDocumentId());
        dto.setUserId(comment.getUserId());
        dto.setParentId(comment.getParentId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        // 获取评论者信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
        }

        // 如果是回复评论，获取被回复用户的信息
        if (comment.getParentId() != null) {
            Comment parentComment = commentService.selectById(comment.getParentId());
            if (parentComment != null) {
                User repliedUser = userMapper.selectById(parentComment.getUserId());
                if (repliedUser != null) {
                    dto.setRepliedUserId(repliedUser.getId());
                    dto.setRepliedUsername(repliedUser.getUsername());
                    dto.setRepliedNickname(repliedUser.getNickname());
                }
            }
        }

        return dto;
    }
}