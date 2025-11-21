package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class DbTestRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== ORACLE DB 연결 테스트 시작 ===");

        String url = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
        String user = "wordweb";
        String password = "1234";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println(">>> DB 연결 성공!");
        } catch (Exception e) {
            System.out.println(">>> DB 연결 실패...");
            e.printStackTrace();
        }
    }
}
