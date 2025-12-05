package com.wordweb.dto.dashboard;

import lombok.Getter;

@Getter
public class WrongTop5Dto {

    private Long wordId;
    private String word;
    private Long count;

    public WrongTop5Dto(Long wordId, String word, Number count) {
        this.wordId = wordId;
        this.word = word;
        this.count = count.longValue();
    }

}
