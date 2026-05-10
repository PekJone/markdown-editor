package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.entity.Blacklist;
import com.markdown.editor.mapper.BlacklistMapper;
import com.markdown.editor.service.BlacklistService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    @Override
    public boolean isBlacklisted(Long userId, Long blockedUserId) {
        QueryWrapper<Blacklist> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("blocked_user_id", blockedUserId);
        return this.getOne(wrapper) != null;
    }

    @Override
    public void addToBlacklist(Long userId, Long blockedUserId) {
        if (!isBlacklisted(userId, blockedUserId)) {
            Blacklist blacklist = new Blacklist();
            blacklist.setUserId(userId);
            blacklist.setBlockedUserId(blockedUserId);
            blacklist.setCreatedAt(new Date());
            this.save(blacklist);
        }
    }

    @Override
    public void removeFromBlacklist(Long userId, Long blockedUserId) {
        QueryWrapper<Blacklist> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("blocked_user_id", blockedUserId);
        this.remove(wrapper);
    }

    @Override
    public List<Long> getBlacklist(Long userId) {
        QueryWrapper<Blacklist> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Blacklist> blacklistEntries = this.list(wrapper);
        List<Long> blacklistIds = new ArrayList<>();
        for (Blacklist entry : blacklistEntries) {
            blacklistIds.add(entry.getBlockedUserId());
        }
        return blacklistIds;
    }

    @Override
    public Blacklist getBlacklistEntry(Long userId, Long blockedUserId) {
        QueryWrapper<Blacklist> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("blocked_user_id", blockedUserId);
        return this.getOne(wrapper);
    }
}
