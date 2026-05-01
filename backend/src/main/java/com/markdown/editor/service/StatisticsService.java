package com.markdown.editor.service;

import com.markdown.editor.dto.UserStatsDTO;

public interface StatisticsService {
    UserStatsDTO calculateUserStats(Long userId);
}