package com.yao.food_menu;

import java.sql.*;

public class CheckConfig {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/food_menu?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT config_key, config_value FROM system_config WHERE config_key LIKE 'ai_%'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("AI Configs in DB:");
                while (rs.next()) {
                    System.out.println("Key: " + rs.getString("config_key") + ", Value: " + rs.getString("config_value"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
