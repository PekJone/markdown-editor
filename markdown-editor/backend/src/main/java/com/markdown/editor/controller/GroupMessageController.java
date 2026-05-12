package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.entity.GroupMessage;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.GroupMessageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/group")
@Api(tags = "群消息管理", description = "群消息发送、获取、未读计数等接口")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;

    @PostMapping
    public ResponseEntity<?> sendGroupMessage(@RequestBody Map<String, Object> request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long groupId = ((Number) request.get("groupId")).longValue();
        String content = (String) request.get("content");
        
        GroupMessage message = groupMessageService.sendMessage(groupId, userDetails.getId(), content);
        return ResponseEntity.ok(ApiResponse.success("发送消息成功", message));
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<?> getGroupMessages(@PathVariable Long groupId) {
        List<Map<String, Object>> messages = groupMessageService.getMessages(groupId);
        return ResponseEntity.ok(ApiResponse.success("获取群消息成功", messages));
    }

    @GetMapping("/{groupId}/unread/count")
    public ResponseEntity<?> getUnreadMessageCount(@PathVariable Long groupId, @RequestParam Long userId) {
        int count = groupMessageService.getUnreadMessageCount(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success("获取未读消息数量成功", count));
    }

    @PostMapping("/{groupId}/read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable Long groupId, @RequestParam Long userId) {
        groupMessageService.markMessagesAsRead(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success("标记消息为已读成功", null));
    }

    @PostMapping("/{groupId}/read/{messageId}")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long groupId, @PathVariable Long messageId, @RequestParam Long userId) {
        groupMessageService.markMessageAsRead(groupId, userId, messageId);
        return ResponseEntity.ok(ApiResponse.success("标记消息为已读成功", null));
    }
}