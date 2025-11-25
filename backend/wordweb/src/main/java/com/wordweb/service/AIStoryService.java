package com.wordweb.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIStoryService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";

    /** ================================================
     *   AI 스토리 생성 (+ 단어 누락 시 자동 재시도)
     * ================================================ */
    public StoryResult generateStory(String[] words, String difficulty, String style) {

        String prompt = buildPrompt(Arrays.asList(words), difficulty, style);

        int maxAttempts = 3;   // 최대 3회 재시도
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;

            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", prompt);

                JSONArray messages = new JSONArray();
                messages.put(userMessage);

                JSONObject requestBodyJson = new JSONObject();
                requestBodyJson.put("model", "deepseek-chat");
                requestBodyJson.put("messages", messages);
                requestBodyJson.put("temperature", 0.7);

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        requestBodyJson.toString()
                );

                Request request = new Request.Builder()
                        .url(DEEPSEEK_URL)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseJson = response.body().string();

                JSONObject jsonObj = new JSONObject(responseJson);

                String rawContent = jsonObj
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 영어/한국어 분리
                String storyEn = extract(rawContent, "[EN]", "[KO]").trim();
                String storyKo = extract(rawContent, "[KO]", null).trim();

                // 단어 포함 여부 분석
                List<String> usedWords = new ArrayList<>();
                boolean allUsed = true;
                String storyLower = storyEn.toLowerCase();

                for (String w : words) {
                    if (storyLower.contains(w.toLowerCase())) {
                        usedWords.add(w);
                    } else {
                        allUsed = false;
                    }
                }

                // 모든 단어 포함 → 성공
                if (allUsed) {
                    return StoryResult.builder()
                            .storyEn(storyEn)
                            .storyKo(storyKo)
                            .usedWords(usedWords)
                            .allWordsUsed(true)
                            .difficulty(difficulty)
                            .style(style)
                            .build();
                }

                // 실패 → 재시도
                System.out.println("[AIStoryService] 단어 누락 → 재시도 " + attempt);

            } catch (Exception e) {

                if (attempt == maxAttempts) {
                    throw new RuntimeException("AI 스토리 생성 실패: " + e.getMessage());
                }
            }
        }

        throw new RuntimeException("AI 스토리 생성 실패: 단어가 반복적으로 누락되었습니다.");
    }

    /** ================================================
     *   프롬프트 생성
     * ================================================ */
    private String buildPrompt(List<String> words, String difficulty, String style) {
        String wordList = String.join(", ", words);

        return """
                You are an English learning assistant.

                Create one short English story that includes ALL of the following vocabulary words:
                %s

                Requirements:
                1. Length: 5–7 sentences.
                2. Style: %s.
                3. Level: %s learner.
                4. The story must be cohesive, natural, and easy to follow.
                5. Every provided word MUST appear at least once.
                6. After the English version, provide a clear and natural Korean translation.
                7. Final output must follow this exact format:

                [EN]
                (English story)

                [KO]
                (Korean translation)
                """.formatted(wordList, style, difficulty);
    }

    /** ================================================
     *   EN/KO 분리 함수
     * ================================================ */
    private String extract(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return "";
        s += start.length();

        if (end == null) return text.substring(s);

        int e = text.indexOf(end);
        if (e == -1) return text.substring(s);

        return text.substring(s, e);
    }

    /** ================================================
     *   StoryResult DTO
     * ================================================ */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoryResult {
        private String storyEn;
        private String storyKo;
        private List<String> usedWords;
        private boolean allWordsUsed;
        private String difficulty;
        private String style;
    }
}

