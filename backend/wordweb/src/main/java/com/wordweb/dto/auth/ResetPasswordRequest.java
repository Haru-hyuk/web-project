package com.wordweb.dto.auth;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String userName;
    private String email;
}
