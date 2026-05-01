package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.MessageVO;
import com.markdown.editor.entity.Comment;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.Message;
import com.markdown.editor.entity.User;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.MessageService;
import com.markdown.editor.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@Api(tags = "消息通知", description = "系统消息通知（点赞、收藏、评论等）的查询、标记已读、删除等接口")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CommentService commentService;

    private MessageVO convertMessageToVO(Message message) {
        MessageVO vo = new MessageVO();
        
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setRelatedId(message.getRelatedId());
        vo.setIsRead(message.getIsRead());
        vo.setCreatedAt(message.getCreatedAt());
        vo.setUpdatedAt(message.getUpdatedAt());
        
        Long senderId = message.getSenderId();
        if (senderId != null && senderId != 0) {
            try {
                User sender = userService.selectById(senderId);
                if (sender != null) {
                    String nickname = sender.getNickname();
                    String username = sender.getUsername();
                    vo.setSenderName(nickname != null && !nickname.isEmpty() ? nickname : username);
                }
            } catch (Exception e) {
            }
        }
        
        Long relatedId = message.getRelatedId();
        String messageType = message.getType();
        if (relatedId != null) {
            try {
                if ("comment".equals(messageType) || "comment_replay".equals(messageType)) {
                    Comment comment = commentService.selectById(relatedId);
                    if (comment != null) {
                        Long commentDocumentId = comment.getDocumentId();
                        if (commentDocumentId != null) {
                            Document document = documentService.selectById(commentDocumentId);
                            if (document != null) {
                                vo.setDocumentId(document.getId());
                                vo.setDocumentTitle(document.getTitle());
                            }
                        } else {
                            Document document = documentService.selectById(relatedId);
                            if (document != null) {
                                vo.setDocumentId(document.getId());
                                vo.setDocumentTitle(document.getTitle());
                            }
                        }
                    } else {
                        Document document = documentService.selectById(relatedId);
                        if (document != null) {
                            vo.setDocumentId(document.getId());
                            vo.setDocumentTitle(document.getTitle());
                        }
                    }
                } else if ("like".equals(messageType) || "collect".equals(messageType)) {
                    Document document = documentService.selectById(relatedId);
                    if (document != null) {
                        vo.setDocumentId(document.getId());
                        vo.setDocumentTitle(document.getTitle());
                    }
                }
            } catch (Exception e) {
            }
        }
        
        return vo;
    }

    @GetMapping
    public ResponseEntity<?> getMessages(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<Message> messages = messageService.getMessagesByReceiverId(userDetails.getId(), page, pageSize);
            long total = messageService.getMessagesCountByReceiverId(userDetails.getId());
            List<MessageVO> messagesWithDetails = messages.stream()
                    .map(this::convertMessageToVO)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", messagesWithDetails);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return ResponseEntity.ok(ApiResponse.success("获取消息列表成功", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取消息列表失败"));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getMessagesByType(@PathVariable String type, Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<Message> messages = messageService.getMessagesByReceiverIdAndType(userDetails.getId(), type, page, pageSize);
            long total = messageService.getMessagesCountByReceiverIdAndType(userDetails.getId(), type);
            List<MessageVO> messagesWithDetails = messages.stream()
                    .map(this::convertMessageToVO)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", messagesWithDetails);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return ResponseEntity.ok(ApiResponse.success("获取消息列表成功", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取消息列表失败"));
        }
    }

    @GetMapping("/types")
    public ResponseEntity<?> getMessagesByTypes(@RequestParam(value = "types", required = true) List<String> types, Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<Message> messages = messageService.getMessagesByReceiverIdAndTypes(userDetails.getId(), types, page, pageSize);
            long total = messageService.getMessagesCountByReceiverIdAndTypes(userDetails.getId(), types);
            List<MessageVO> messagesWithDetails = messages.stream()
                    .map(this::convertMessageToVO)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", messagesWithDetails);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return ResponseEntity.ok(ApiResponse.success("获取消息列表成功", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取消息列表失败"));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadMessageCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        int count = messageService.getUnreadMessageCount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取未读消息数量成功", count));
    }

    @PutMapping("/read")
    public ResponseEntity<?> markMessagesAsRead(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        messageService.markMessagesAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("标记消息为已读成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        messageService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("删除消息成功", null));
    }
}
