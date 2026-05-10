package com.markdown.editor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.markdown.editor.entity.Blacklist;

public interface BlacklistService extends IService<Blacklist> {
    boolean isBlacklisted(Long userId, Long blockedUserId);
    void addToBlacklist(Long userId, Long blockedUserId);
    void removeFromBlacklist(Long userId, Long blockedUserId);
    java.util.List<Long> getBlacklist(Long userId);
    Blacklist getBlacklistEntry(Long userId, Long blockedUserId);
}