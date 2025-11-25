package com.wordweb.dto.progress;

import lombok.Getter;

@Getter
public class ProgressUpdateRequest {
    private String status;  // IN_PROGRESS / DONE
}
