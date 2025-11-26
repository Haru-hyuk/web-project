package com.wordweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * 임시 비밀번호 생성
     */
    public String generateTempPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 실제 이메일 전송
     */
    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * 비밀번호 찾기용 임시 비밀번호 발송
     */
    public String sendTempPassword(String toEmail) {
        String tempPassword = generateTempPassword();

        String subject = "[WordWeb] 임시 비밀번호 안내";
        String body =
                "안녕하세요.\n\n" +
                "요청하신 임시 비밀번호는 아래와 같습니다.\n\n" +
                "임시 비밀번호: " + tempPassword + "\n\n" +
                "로그인 후 반드시 비밀번호를 변경해주세요.\n\n" +
                "감사합니다.";

        sendMail(toEmail, subject, body);

        return tempPassword;
    }
}
