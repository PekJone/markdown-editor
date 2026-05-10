package com.markdown.editor.service.impl;

import com.markdown.editor.entity.GroupChat;
import com.markdown.editor.entity.GroupMember;
import com.markdown.editor.entity.GroupMessage;
import com.markdown.editor.entity.User;
import com.markdown.editor.mapper.GroupChatMapper;
import com.markdown.editor.mapper.GroupMemberMapper;
import com.markdown.editor.mapper.UserMapper;
import com.markdown.editor.service.GroupChatService;
import com.markdown.editor.service.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class GroupChatServiceImpl implements GroupChatService {

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMessageService groupMessageService;

    private static final String[] AVATAR_COLORS = {
        "#667eea", "#764ba2", "#f093fb", "#f5576c", "#4facfe",
        "#00f2fe", "#43e97b", "#38f9d7", "#fa709a", "#fee140"
    };

    private String generateDefaultAvatar(String groupName) {
        String color = AVATAR_COLORS[new Random().nextInt(AVATAR_COLORS.length)];
        String initial = groupName != null && !groupName.isEmpty() ? groupName.charAt(0) + "" : "G";
        return "https://api.dicebear.com/7.x/pixel-art/svg?seed=" + encodeURIComponent(groupName != null ? groupName : "group") + "&bg=" + color.replace("#", "");
    }
    
    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }

    @Override
    @Transactional
    public GroupChat createGroup(Long creatorId, String name, String description, List<Long> memberIds) {
        GroupChat group = new GroupChat();
        group.setName(name);
        group.setDescription(description);
        group.setCreatorId(creatorId);
        group.setMemberCount(memberIds.size() + 1);
        group.setAvatar(generateDefaultAvatar(name));
        group.setDeleted(false);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupChatMapper.insert(group);

        GroupMember creator = new GroupMember();
        creator.setGroupId(group.getId());
        creator.setUserId(creatorId);
        creator.setIsOwner(true);
        creator.setIsAdmin(true);
        creator.setIsDeleted(false);
        creator.setJoinedAt(new Date());
        groupMemberMapper.insert(creator);

        for (Long memberId : memberIds) {
            GroupMember member = new GroupMember();
            member.setGroupId(group.getId());
            member.setUserId(memberId);
            member.setIsOwner(false);
            member.setIsAdmin(false);
            member.setIsDeleted(false);
            member.setJoinedAt(new Date());
            groupMemberMapper.insert(member);
        }

        return group;
    }

    @Override
    public GroupChat getGroupById(Long groupId) {
        return groupChatMapper.selectById(groupId);
    }

    @Override
    public List<GroupChat> getGroupsByUserId(Long userId) {
        List<GroupMember> memberships = groupMemberMapper.selectByUserId(userId);
        return memberships.stream()
                .filter(m -> m.getIsDeleted() == null || !m.getIsDeleted())
                .map(m -> {
                    GroupChat group = groupChatMapper.selectById(m.getGroupId());
                    if (group != null && (group.getDeleted() == null || !group.getDeleted())) {
                        if (group.getMemberCount() != null && group.getMemberCount() <= 1) {
                            group.setDeleted(true);
                            groupChatMapper.updateById(group);
                            return null;
                        }
                        return group;
                    }
                    return null;
                })
                .filter(g -> g != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(Long groupId, Long userId) {
        GroupMember existing = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (existing != null) {
            existing.setIsDeleted(false);
            existing.setLeftAt(null);
            groupMemberMapper.updateById(existing);
        } else {
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setIsOwner(false);
            member.setIsAdmin(false);
            member.setIsDeleted(false);
            member.setJoinedAt(new Date());
            groupMemberMapper.insert(member);
            
            GroupChat group = groupChatMapper.selectById(groupId);
            if (group != null) {
                group.setMemberCount(group.getMemberCount() + 1);
                groupChatMapper.updateById(group);
            }
        }
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId, Long operatorId) {
        if (!hasManagePermission(groupId, operatorId)) {
            throw new RuntimeException("没有权限执行此操作");
        }
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            boolean isOwner = member.getIsOwner() != null && member.getIsOwner();
            member.setIsDeleted(true);
            member.setLeftAt(new Date());
            groupMemberMapper.updateById(member);

            GroupChat group = groupChatMapper.selectById(groupId);
            if (group != null) {
                int newMemberCount = group.getMemberCount() - 1;
                group.setMemberCount(newMemberCount);
                
                if (isOwner) {
                    Long newOwnerId = findNewOwner(groupId, userId);
                    if (newOwnerId != null) {
                        group.setCreatorId(newOwnerId);
                        
                        GroupMember newOwner = groupMemberMapper.selectByGroupIdAndUserId(groupId, newOwnerId);
                        if (newOwner != null) {
                            newOwner.setIsOwner(true);
                            newOwner.setIsAdmin(false);
                            groupMemberMapper.updateById(newOwner);
                        }
                    } else {
                        group.setDeleted(true);
                    }
                } else if (newMemberCount <= 1) {
                    group.setDeleted(true);
                }
                
                groupChatMapper.updateById(group);
            }
        }
    }

    @Override
    public void updateGroup(Long groupId, String name, String description) {
        GroupChat group = groupChatMapper.selectById(groupId);
        if (group != null) {
            if (name != null) group.setName(name);
            if (description != null) group.setDescription(description);
            group.setUpdatedAt(new Date());
            groupChatMapper.updateById(group);
        }
    }

    @Override
    @Transactional
    public void updateAnnouncement(Long groupId, String announcement, Long publisherId) {
        GroupChat group = groupChatMapper.selectById(groupId);
        if (group != null) {
            group.setAnnouncement(announcement);
            group.setUpdatedAt(new Date());
            groupChatMapper.updateById(group);
            
            String messageContent = "📢 群公告已更新：\n" + announcement;
            GroupMessage message = groupMessageService.sendMessage(groupId, 0L, messageContent);
            
            if (publisherId != null && publisherId > 0) {
                groupMessageService.markMessageAsRead(groupId, publisherId, message.getId());
            }
        }
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        GroupChat group = groupChatMapper.selectById(groupId);
        if (group != null && userId.equals(group.getCreatorId())) {
            group.setDeleted(true);
            groupChatMapper.updateById(group);

            List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId);
            for (GroupMember member : members) {
                member.setIsDeleted(true);
                member.setLeftAt(new Date());
                groupMemberMapper.updateById(member);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId).stream()
                .filter(m -> m.getIsDeleted() == null || !m.getIsDeleted())
                .collect(Collectors.toList());
        
        List<Map<String, Object>> result = members.stream().map(member -> {
            Map<String, Object> map = new HashMap<>();
            // 前端需要 id 字段
            map.put("id", member.getUserId());
            map.put("userId", member.getUserId());
            map.put("groupId", member.getGroupId());
            map.put("nickname", member.getNickname());
            map.put("isAdmin", member.getIsAdmin());
            map.put("isOwner", member.getIsOwner());
            map.put("joinedAt", member.getJoinedAt());
            
            // 获取用户信息
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                map.put("avatar", user.getAvatar());
                map.put("username", user.getUsername());
                // 如果群成员昵称为空，使用用户昵称
                if (member.getNickname() == null || member.getNickname().isEmpty()) {
                    map.put("nickname", user.getNickname());
                }
            }
            
            return map;
        }).collect(Collectors.toList());
        
        return result;
    }
    
    @Override
    @Transactional
    public void setAdmin(Long groupId, Long userId, Long operatorId) {
        if (!hasManagePermission(groupId, operatorId)) {
            throw new RuntimeException("没有权限执行此操作");
        }
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member != null && (member.getIsOwner() == null || !member.getIsOwner())) {
            int adminCount = getAdminCount(groupId);
            if (adminCount >= 3) {
                throw new RuntimeException("管理员数量已达上限（最多3个）");
            }
            member.setIsAdmin(true);
            groupMemberMapper.updateById(member);
        }
    }
    
    @Override
    @Transactional
    public void removeAdmin(Long groupId, Long userId, Long operatorId) {
        if (!hasManagePermission(groupId, operatorId)) {
            throw new RuntimeException("没有权限执行此操作");
        }
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member != null && (member.getIsAdmin() != null && member.getIsAdmin())) {
            member.setIsAdmin(false);
            groupMemberMapper.updateById(member);
        }
    }
    
    @Override
    public int getAdminCount(Long groupId) {
        List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId).stream()
                .filter(m -> m.getIsDeleted() == null || !m.getIsDeleted())
                .filter(m -> m.getIsAdmin() != null && m.getIsAdmin())
                .filter(m -> m.getIsOwner() == null || !m.getIsOwner())
                .collect(Collectors.toList());
        return members.size();
    }
    
    private boolean hasManagePermission(Long groupId, Long userId) {
        if (userId == null) {
            return false;
        }
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null || (member.getIsDeleted() != null && member.getIsDeleted())) {
            return false;
        }
        return (member.getIsOwner() != null && member.getIsOwner()) || 
               (member.getIsAdmin() != null && member.getIsAdmin());
    }
    
    private Long findNewOwner(Long groupId, Long leavingOwnerId) {
        List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId).stream()
                .filter(m -> m.getIsDeleted() == null || !m.getIsDeleted())
                .filter(m -> !leavingOwnerId.equals(m.getUserId()))
                .collect(Collectors.toList());
        
        if (members.isEmpty()) {
            return null;
        }
        
        Optional<GroupMember> adminMember = members.stream()
                .filter(m -> m.getIsAdmin() != null && m.getIsAdmin())
                .findFirst();
        
        if (adminMember.isPresent()) {
            return adminMember.get().getUserId();
        }
        
        return members.stream()
                .min(Comparator.comparing(GroupMember::getJoinedAt))
                .map(GroupMember::getUserId)
                .orElse(null);
    }
}