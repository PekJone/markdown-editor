package com.markdown.editor.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UpdateDatabasePassword {
    public static void main(String[] args) {
        try {
            // Generate password hashes
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            String adminPassword = "admin123";
            String adminHash = encoder.encode(adminPassword);
            System.out.println("Admin password: " + adminPassword);
            System.out.println("Admin hash: " + adminHash);
            
            String testPassword = "test123";
            String testHash = encoder.encode(testPassword);
            System.out.println("\nTest password: " + testPassword);
            System.out.println("Test hash: " + testHash);
            
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to database
            String url = "jdbc:mysql://39.107.242.71:3346/markdown_editor?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
            String username = "root";
            String password = "123456";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            
            // Update admin password
            String updateAdmin = "UPDATE users SET password = '" + adminHash + "' WHERE username = 'admin'";
            int adminResult = stmt.executeUpdate(updateAdmin);
            System.out.println("\nUpdated admin password: " + adminResult + " row(s) affected");
            
            // Update test user password
            String updateTest = "UPDATE users SET password = '" + testHash + "' WHERE username = 'test'";
            int testResult = stmt.executeUpdate(updateTest);
            System.out.println("Updated test password: " + testResult + " row(s) affected");
            
            // Close connection
            stmt.close();
            conn.close();
            
            System.out.println("\nPassword update completed successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
