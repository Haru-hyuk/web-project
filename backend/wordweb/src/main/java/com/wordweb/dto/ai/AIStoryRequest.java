package com.wordweb.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIStoryRequest {

    // WRONG_ANSWER_LOG 테이블의 PK(WRONG_WORD_ID) 목록
    private Long[] wrongAnswerLogIds;

    private String difficulty;
    private String style;
}
