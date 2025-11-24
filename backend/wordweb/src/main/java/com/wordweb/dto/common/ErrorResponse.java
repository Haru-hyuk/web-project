package com.wordweb.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private boolean success;
    private String message;
    private int status;
}
