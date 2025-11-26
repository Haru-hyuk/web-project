package com.wordweb.dto.user;

import lombok.Getter;

@Getter
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
