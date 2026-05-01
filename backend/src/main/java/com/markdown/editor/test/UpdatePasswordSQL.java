package com.markdown.editor.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdatePasswordSQL {
    public static void main(String[] args) {
        try {
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to database
            String url = "jdbc:mysql://39.107.242.71:3346/markdown_editor?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
            String username = "root";
            String password = "123456";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            
            // Update admin password
            String updateAdmin = "UPDATE users SET password = '$2a$10$Ro7jcgrTZfIvKFufF0bSG.zFiBJFdJnggmhbQwXcpMW90RPvfbaVC' WHERE username = 'admin'";
            int adminResult = stmt.executeUpdate(updateAdmin);
            System.out.println("Updated admin password: " + adminResult + " row(s) affected");
            
            // Update test user password
            String updateTest = "UPDATE users SET password = '$2a$10$S9IHxtBlRZA0ON/PlUVG0OjR/Uk.jsrVBIDb8JE/IMErI5Snp1wJ6' WHERE username = 'test'";
            int testResult = stmt.executeUpdate(updateTest);
            System.out.println("Updated test password: " + testResult + " row(s) affected");
            
            // Close connection
            stmt.close();
            conn.close();
            
            System.out.println("Password update completed successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
