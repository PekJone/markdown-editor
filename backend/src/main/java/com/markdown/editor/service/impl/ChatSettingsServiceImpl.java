package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.entity.ChatSettings;
import com.markdown.editor.mapper.ChatSettingsMapper;
import com.markdown.editor.service.ChatSettingsService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ChatSettingsServiceImpl extends ServiceImpl<ChatSettingsMapper, ChatSettings> implements ChatSettingsService {

    @Override
    public ChatSettings getChatSettings(Long userId, Long chatPartnerId) {
        QueryWrapper<ChatSettings> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("chat_partner_id", chatPartnerId);
        return this.getOne(wrapper);
    }

    @Override
    public void setPinned(Long userId, Long chatPartnerId, Boolean isPinned) {
        ChatSettings settings = getChatSettings(userId, chatPartnerId);
        if (settings == null) {
            settings = new ChatSettings();
            settings.setUserId(userId);
            settings.setChatPartnerId(chatPartnerId);
            settings.setIsPinned(isPinned);
            settings.setDoNotDisturb(false);
            settings.setDeleted(false);
            settings.setCreatedAt(new Date());
            settings.setUpdatedAt(new Date());
            this.save(settings);
        } else {
            settings.setIsPinned(isPinned);
            settings.setUpdatedAt(new Date());
            this.updateById(settings);
        }
    }

    @Override
    public void setDoNotDisturb(Long userId, Long chatPartnerId, Boolean doNotDisturb) {
        ChatSettings settings = getChatSettings(userId, chatPartnerId);
        if (settings == null) {
            settings = new ChatSettings();
            settings.setUserId(userId);
            settings.setChatPartnerId(chatPartnerId);
            settings.setIsPinned(false);
            settings.setDoNotDisturb(doNotDisturb);
            settings.setDeleted(false);
            settings.setCreatedAt(new Date());
            settings.setUpdatedAt(new Date());
            this.save(settings);
        } else {
            settings.setDoNotDisturb(doNotDisturb);
            settings.setUpdatedAt(new Date());
            this.updateById(settings);
        }
    }

    @Override
    public void deleteConversation(Long userId, Long chatPartnerId) {
        ChatSettings settings = getChatSettings(userId, chatPartnerId);
        if (settings == null) {
            settings = new ChatSettings();
            settings.setUserId(userId);
            settings.setChatPartnerId(chatPartnerId);
            settings.setIsPinned(false);
            settings.setDoNotDisturb(false);
            settings.setDeleted(true);
            settings.setDeletedAt(new Date());
            settings.setCreatedAt(new Date());
            settings.setUpdatedAt(new Date());
            this.save(settings);
        } else {
            settings.setDeleted(true);
            settings.setDeletedAt(new Date());
            settings.setUpdatedAt(new Date());
            this.updateById(settings);
        }
    }

    @Override
    public boolean isConversationDeletedByUser(Long userId, Long chatPartnerId) {
        ChatSettings settings = getChatSettings(userId, chatPartnerId);
        return settings != null && settings.getDeleted() != null && settings.getDeleted();
    }
}
