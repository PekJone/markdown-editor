package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.ChatUserDTO;
import com.markdown.editor.dto.UserInfoVO;
import com.markdown.editor.entity.ChatMessage;
import com.markdown.editor.entity.ChatSettings;
import com.markdown.editor.entity.Blacklist;
import com.markdown.editor.entity.User;
import com.markdown.editor.mapper.UserMapper;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.BlacklistService;
import com.markdown.editor.service.ChatMessageService;
import com.markdown.editor.service.ChatSettingsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Api(tags = "聊天管理", description = "一对一聊天、黑名单、免打扰、置顶等接口")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatSettingsService chatSettingsService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public ResponseEntity<?> sendChatMessage(@RequestBody ChatMessage message, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        message.setSenderId(userDetails.getId());
        Long receiverId = message.getReceiverId();
        
        if (blacklistService.isBlacklisted(receiverId, userDetails.getId())) {
            return ResponseEntity.ok(ApiResponse.error("您已被对方拉黑，无法发送消息"));
        }
        
        ChatSettings settings = chatSettingsService.getChatSettings(userDetails.getId(), receiverId);
        if (settings != null) {
            if (settings.getDeleted() != null && settings.getDeleted()) {
                chatSettingsService.removeById(settings.getId());
                
                ChatSettings newSettings = new ChatSettings();
                newSettings.setUserId(userDetails.getId());
                newSettings.setChatPartnerId(receiverId);
                newSettings.setIsPinned(false);
                newSettings.setDoNotDisturb(false);
                newSettings.setDeleted(false);
                newSettings.setCreatedAt(new Date());
                newSettings.setUpdatedAt(new Date());
                chatSettingsService.save(newSettings);
            }
        } else {
            ChatSettings newSettings = new ChatSettings();
            newSettings.setUserId(userDetails.getId());
            newSettings.setChatPartnerId(receiverId);
            newSettings.setIsPinned(false);
            newSettings.setDoNotDisturb(false);
            newSettings.setDeleted(false);
            newSettings.setCreatedAt(new Date());
            newSettings.setUpdatedAt(new Date());
            chatSettingsService.save(newSettings);
        }
        
        chatMessageService.sendChatMessage(message);
        return ResponseEntity.ok(ApiResponse.success("发送消息成功", message));
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getChatMessages(@RequestParam Long receiverId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ChatMessage> messages = chatMessageService.getChatMessages(userDetails.getId(), receiverId);
        chatMessageService.markChatMessagesAsRead(receiverId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取聊天记录成功", messages));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadChatMessageCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        int count = chatMessageService.getUnreadChatMessageCount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取未读消息数量成功", count));
    }

    @PutMapping("/read")
    public ResponseEntity<?> markChatMessagesAsRead(@RequestParam Long senderId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        chatMessageService.markChatMessagesAsRead(senderId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("标记消息为已读成功", null));
    }

    @GetMapping("/messages/list")
    public ResponseEntity<?> getChatUserList(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ChatUserDTO> chatList = chatMessageService.getChatList(userDetails.getId());
        
        List<ChatUserDTO> filteredChatList = new ArrayList<>();
        for (ChatUserDTO chatUser : chatList) {
            Long chatUserId = chatUser.getId();
            
            ChatSettings settings = chatSettingsService.getChatSettings(userDetails.getId(), chatUserId);
            if (settings != null) {
                chatUser.setIsPinned(settings.getIsPinned() != null ? settings.getIsPinned() : false);
                chatUser.setDoNotDisturb(settings.getDoNotDisturb() != null ? settings.getDoNotDisturb() : false);
                
                if (settings.getDeleted() == null || !settings.getDeleted()) {
                    chatUser.setIsBlacklisted(blacklistService.isBlacklisted(userDetails.getId(), chatUserId));
                    filteredChatList.add(chatUser);
                }
            } else {
                chatUser.setIsPinned(false);
                chatUser.setDoNotDisturb(false);
                chatUser.setIsBlacklisted(blacklistService.isBlacklisted(userDetails.getId(), chatUserId));
                filteredChatList.add(chatUser);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("获取聊天用户列表成功", filteredChatList));
    }

    @PutMapping("/pin")
    public ResponseEntity<?> setPinnedChat(@RequestParam Long chatPartnerId, @RequestParam Boolean isPinned, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        chatSettingsService.setPinned(userDetails.getId(), chatPartnerId, isPinned);
        return ResponseEntity.ok(ApiResponse.success(isPinned ? "置顶成功" : "取消置顶成功", null));
    }

    @PutMapping("/dnd")
    public ResponseEntity<?> setDoNotDisturb(@RequestParam Long chatPartnerId, @RequestParam Boolean doNotDisturb, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        chatSettingsService.setDoNotDisturb(userDetails.getId(), chatPartnerId, doNotDisturb);
        return ResponseEntity.ok(ApiResponse.success(doNotDisturb ? "已开启免打扰" : "已关闭免打扰", null));
    }

    @PostMapping("/blacklist")
    public ResponseEntity<?> addToBlacklist(@RequestParam Long blockedUserId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        blacklistService.addToBlacklist(userDetails.getId(), blockedUserId);
        return ResponseEntity.ok(ApiResponse.success("已加入黑名单", null));
    }

    @DeleteMapping("/blacklist")
    public ResponseEntity<?> removeFromBlacklist(@RequestParam Long blockedUserId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        blacklistService.removeFromBlacklist(userDetails.getId(), blockedUserId);
        return ResponseEntity.ok(ApiResponse.success("已移出黑名单", null));
    }

    @GetMapping("/blacklist/check")
    public ResponseEntity<?> isBlacklisted(@RequestParam Long userId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isBlacklisted = blacklistService.isBlacklisted(userDetails.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("查询成功", isBlacklisted));
    }

    @GetMapping("/blacklist")
    public ResponseEntity<?> getBlacklist(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Long> blacklistIds = blacklistService.getBlacklist(userDetails.getId());
        List<UserInfoVO> blacklist = new ArrayList<>();

        for (Long blacklistId : blacklistIds) {
            User user = userMapper.selectById(blacklistId);
            if (user != null) {
                UserInfoVO userInfo = new UserInfoVO();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setNickname(user.getNickname());
                userInfo.setAvatar(user.getAvatar());
                
                Blacklist blacklistEntity = blacklistService.getBlacklistEntry(userDetails.getId(), blacklistId);
                if (blacklistEntity != null) {
                    userInfo.setCreatedAt(blacklistEntity.getCreatedAt());
                }
                
                blacklist.add(userInfo);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("获取黑名单列表成功", blacklist));
    }

    @DeleteMapping("/conversation")
    public ResponseEntity<?> deleteChatConversation(@RequestParam Long partnerId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        chatSettingsService.deleteConversation(userDetails.getId(), partnerId);
        chatMessageService.deleteChatConversation(userDetails.getId(), partnerId);
        return ResponseEntity.ok(ApiResponse.success("删除对话成功", null));
    }
}
