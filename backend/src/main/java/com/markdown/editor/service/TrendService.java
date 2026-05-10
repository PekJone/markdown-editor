package com.markdown.editor.service;

import com.markdown.editor.dto.TrendDataDTO;

import java.time.LocalDate;
import java.util.List;

public interface TrendService {
    
    List<TrendDataDTO> getTrendData(Long userId, LocalDate startDate, LocalDate endDate);
}