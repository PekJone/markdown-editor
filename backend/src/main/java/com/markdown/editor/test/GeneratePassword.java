package com.markdown.editor.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成admin123的哈希值
        String adminPassword = "admin123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Admin password: " + adminPassword);
        System.out.println("Admin hash: " + adminHash);
        
        // 生成test123的哈希值
        String testPassword = "test123";
        String testHash = encoder.encode(testPassword);
        System.out.println("\nTest password: " + testPassword);
        System.out.println("Test hash: " + testHash);
        
        // 生成SQL更新语句
        System.out.println("\nSQL Update statements:");
        System.out.println("UPDATE users SET password = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + testHash + "' WHERE username = 'test';");
    }
}
