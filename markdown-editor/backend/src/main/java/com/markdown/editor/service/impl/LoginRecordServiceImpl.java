package com.markdown.editor.service.impl;

import com.markdown.editor.entity.LoginRecord;
import com.markdown.editor.mapper.LoginRecordMapper;
import com.markdown.editor.service.LoginRecordService;
import com.markdown.editor.util.IpLocationUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginRecordServiceImpl implements LoginRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LoginRecordServiceImpl.class);
    
    @Autowired
    private LoginRecordMapper loginRecordMapper;

    @Override
    @Async("taskExecutor")
    public void recordLogin(Long userId, String ipAddress, String userAgent) {
        try {
            LoginRecord record = new LoginRecord();
            record.setUserId(userId);
            
            String realIp = normalizeIpAddress(ipAddress);
            
            record.setIpAddress(realIp);
            record.setLoginTime(LocalDateTime.now());
            
            UserAgent ua = UserAgent.parseUserAgentString(userAgent);
            record.setBrowser(ua.getBrowser().getName());
            record.setDevice(ua.getOperatingSystem().getName());
            
            record.setLocation(getLocationFromIp(realIp));
            
            loginRecordMapper.insert(record);
            logger.debug("登录记录已保存，用户ID: {}, IP: {}", userId, realIp);
        } catch (Exception e) {
            logger.error("异步记录登录失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<LoginRecord> getLoginRecords(Long userId) {
        return loginRecordMapper.findAllByUserIdOrderByLoginTimeDesc(userId);
    }

    @Override
    public List<LoginRecord> getRecentLoginRecords(Long userId, int limit) {
        return loginRecordMapper.findByUserIdOrderByLoginTimeDesc(userId, limit);
    }

    private String normalizeIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "未知";
        }
        
        ip = ip.trim();
        
        if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
            return "127.0.0.1";
        }
        
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    private boolean isLocalIp(String ip) {
        if (ip == null || ip.isEmpty() || "未知".equals(ip)) {
            return true;
        }
        
        return ip.equals("127.0.0.1") 
            || ip.startsWith("192.168") 
            || ip.startsWith("10.") 
            || ip.startsWith("172.16") 
            || ip.startsWith("172.17") 
            || ip.startsWith("172.18") 
            || ip.startsWith("172.19") 
            || ip.startsWith("172.20") 
            || ip.startsWith("172.21") 
            || ip.startsWith("172.22") 
            || ip.startsWith("172.23") 
            || ip.startsWith("172.24") 
            || ip.startsWith("172.25") 
            || ip.startsWith("172.26") 
            || ip.startsWith("172.27") 
            || ip.startsWith("172.28") 
            || ip.startsWith("172.29") 
            || ip.startsWith("172.30") 
            || ip.startsWith("172.31") 
            || ip.startsWith("::1") 
            || ip.startsWith("fe80:")
            || ip.equals("localhost");
    }

    private String getLocationFromIp(String ip) {
        if (ip == null || ip.isEmpty() || "未知".equals(ip)) {
            return "未知";
        }
        
        if (isLocalIp(ip)) {
            return "本地网络";
        }
        
        String location = IpLocationUtil.getLocation(ip);
        if (location != null && !location.isEmpty()) {
            return location;
        }
        
        return "未知位置";
    }
}