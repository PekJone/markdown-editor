package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.entity.GroupChat;
import com.markdown.editor.entity.GroupMember;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.GroupChatService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
@Api(tags = "群聊管理", description = "群聊创建、成员管理等接口")
public class GroupChatController {

    @Autowired
    private GroupChatService groupChatService;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            
            // 处理memberIds，确保不为null
            List<Long> memberIds = null;
            Object memberIdsObj = request.get("memberIds");
            if (memberIdsObj instanceof List) {
                memberIds = ((List<?>) memberIdsObj).stream()
                    .map(id -> {
                        if (id instanceof Long) {
                            return (Long) id;
                        } else if (id instanceof Number) {
                            return ((Number) id).longValue();
                        } else if (id instanceof String) {
                            return Long.parseLong((String) id);
                        }
                        return null;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                memberIds = new java.util.ArrayList<>();
            }

            GroupChat group = groupChatService.createGroup(userDetails.getId(), name, description, memberIds);
            return ResponseEntity.ok(ApiResponse.success("创建群聊成功", group));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("创建群聊失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable Long groupId) {
        GroupChat group = groupChatService.getGroupById(groupId);
        if (group == null) {
            return ResponseEntity.ok(ApiResponse.error("群聊不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("获取群聊成功", group));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyGroups(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<GroupChat> groups = groupChatService.getGroupsByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取群聊列表成功", groups));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {
        List<?> members = groupChatService.getGroupMembers(groupId);
        return ResponseEntity.ok(ApiResponse.success("获取群成员列表成功", members));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMembers(@PathVariable Long groupId, @RequestBody Map<String, List<Long>> request) {
        List<Long> memberIds = request.get("memberIds");
        if (memberIds != null && !memberIds.isEmpty()) {
            for (Long userId : memberIds) {
                groupChatService.addMember(groupId, userId);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("添加成员成功", null));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId, @PathVariable Long userId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            groupChatService.removeMember(groupId, userId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("移除成员成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        groupChatService.removeMember(groupId, userDetails.getId(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("退出群聊成功", null));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable Long groupId, @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        groupChatService.updateGroup(groupId, name, description);
        return ResponseEntity.ok(ApiResponse.success("更新群聊成功", null));
    }

    @PutMapping("/{groupId}/announcement")
    public ResponseEntity<?> updateAnnouncement(@PathVariable Long groupId, @RequestBody Map<String, Object> request, Authentication authentication) {
        String announcement = (String) request.get("announcement");
        Long publisherId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            publisherId = userDetails.getId();
        }
        groupChatService.updateAnnouncement(groupId, announcement, publisherId);
        return ResponseEntity.ok(ApiResponse.success("更新群公告成功", null));
    }

    @PostMapping("/{groupId}/admin/{userId}")
    public ResponseEntity<?> setAdmin(@PathVariable Long groupId, @PathVariable Long userId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            groupChatService.setAdmin(groupId, userId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("设置管理员成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/admin/{userId}")
    public ResponseEntity<?> removeAdmin(@PathVariable Long groupId, @PathVariable Long userId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            groupChatService.removeAdmin(groupId, userId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("移除管理员成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/admin/count")
    public ResponseEntity<?> getAdminCount(@PathVariable Long groupId) {
        int count = groupChatService.getAdminCount(groupId);
        return ResponseEntity.ok(ApiResponse.success("获取管理员数量成功", count));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        groupChatService.deleteGroup(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("删除群聊成功", null));
    }
}