package com.wordweb.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    private String currentPassword;     // 현재 비밀번호
    private String newPassword;         // 새로운 비밀번호
    private String confirmNewPassword;  // 새로운 비밀번호 확인
}
