package com.markdown.editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDTO {
    
    private LocalDate date;
    
    private Integer views;
    
    private Integer comments;
    
    private Integer followers;
    
    private Integer collections;
}