package com.wordweb.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private String nickname;
    private String userName;
    private String userBirth;
}
