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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            if (comment.getParentId() != null) {
                LogUtils.info(logger, "用户[{}]回复评论，父评论ID: {}", userDetails.getUsername(), comment.getParentId());
            }

            int result = commentService.insert(comment);
            if (result > 0) {
                LogUtils.info(logger, "用户[{}]添加评论成功，评论ID: {}", userDetails.getUsername(), comment.getId());
                
                Document document = documentService.selectById(comment.getDocumentId());
                if (document != null) {
                    if (!document.getUserId().equals(userDetails.getId())) {
                        Message message = new Message();
                        message.setSenderId(userDetails.getId());
                        message.setReceiverId(document.getUserId());
                        message.setType("comment");
                        message.setContent("您的文章《" + document.getTitle() + "》收到了新评论");
                        message.setRelatedId(comment.getId());
                        messageService.sendMessage(message);
                    }
                    
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

            boolean isAdmin = SystemConstant.ROLE_ADMIN.equals(userDetails.getRole());
            
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
            CommentService.CommentListResult result = commentService.getMyCommentsWithDetails(userDetails.getId(), page, size);

            LogUtils.info(logger, "用户[{}]获取自己的评论列表成功，共{}条评论，当前页{}条", userDetails.getUsername(), result.getTotal(), result.getRecords().size());
            return ResponseEntity.ok(result);
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
            CommentService.CommentListResult result = commentService.getReceivedCommentsWithDetails(userDetails.getId(), page, size);

            LogUtils.info(logger, "用户[{}]获取收到的评论列表成功，共{}条评论，当前页{}条", userDetails.getUsername(), result.getTotal(), result.getRecords().size());
            return ResponseEntity.ok(result);
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

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
        }

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
