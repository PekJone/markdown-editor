package com.markdown.editor.service;

import com.markdown.editor.entity.LoginRecord;

import java.util.List;

public interface LoginRecordService {
    
    void recordLogin(Long userId, String ipAddress, String userAgent);
    
    List<LoginRecord> getLoginRecords(Long userId);
    
    List<LoginRecord> getRecentLoginRecords(Long userId, int limit);
}