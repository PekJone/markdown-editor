package com.markdown.editor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.markdown.editor.entity.ChatSettings;

public interface ChatSettingsService extends IService<ChatSettings> {
    ChatSettings getChatSettings(Long userId, Long chatPartnerId);
    void setPinned(Long userId, Long chatPartnerId, Boolean isPinned);
    void setDoNotDisturb(Long userId, Long chatPartnerId, Boolean doNotDisturb);
    void deleteConversation(Long userId, Long chatPartnerId);
    boolean isConversationDeletedByUser(Long userId, Long chatPartnerId);
}