package com.markdown.editor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_records")
public class LoginRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String ipAddress;
    
    private String location;
    
    private String device;
    
    private String browser;
    
    private LocalDateTime loginTime;
}