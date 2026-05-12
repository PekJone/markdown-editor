package com.markdown.editor.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UpdatePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成新的密码哈希
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("New encoded password for 'admin123':");
        System.out.println(encodedPassword);
        
        // 生成测试用户密码哈希
        String testPassword = "test123";
        String testEncodedPassword = encoder.encode(testPassword);
        
        System.out.println("\nNew encoded password for 'test123':");
        System.out.println(testEncodedPassword);
        
        // 生成SQL更新语句
        System.out.println("\nSQL Update statements:");
        System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + testEncodedPassword + "' WHERE username = 'test';");
    }
}
