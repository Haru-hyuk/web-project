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
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AIStoryService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    // DeepSeek API 엔드포인트
    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";

    /** ================================================
     *   AI 스토리 생성 (+ 단어 누락 시 자동 재시도)
     * ================================================ */
    public StoryResult generateStory(String[] words, String difficulty, String style) {

        String prompt = buildPrompt(Arrays.asList(words), difficulty, style);

        int maxAttempts = 3;
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;

            try {
                /** 🔥 타임아웃 넉넉하게 설정된 OkHttpClient */
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(90, TimeUnit.SECONDS)
                        .callTimeout(120, TimeUnit.SECONDS)
                        .build();

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

                /** 🔵 Raw Response 로그 출력 */
                System.out.println("\n================ RAW DEEPSEEK RESPONSE ================");
                System.out.println(responseJson);
                System.out.println("=======================================================\n");

                JSONObject jsonObj = new JSONObject(responseJson);

                // DeepSeek 응답 구조 변환
                String rawContent = jsonObj
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 영어/한국어 분리
                String storyEn = extract(rawContent, "[EN]", "[KO]").trim();
                String storyKo = extract(rawContent, "[KO]", null).trim();

                // 단어 사용 여부 체크
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

                // 모든 단어 사용 성공
                if (allUsed) {
                    return new StoryResult(true, storyEn, storyKo, usedWords);
                }

                // 실패 시 자동 재시도
                System.out.println("❗ 일부 단어가 사용되지 않음. 재시도 중... (" + attempt + "/" + maxAttempts + ")");

            } catch (Exception e) {
                System.out.println("❌ DeepSeek 에러: " + e.getMessage());
            }
        }

        return new StoryResult(false, "AI 스토리 생성 실패", "AI 스토리 생성 실패", Arrays.asList());
    }

    /** ================================================
     *   프롬프트 생성
     * ================================================ */
    private String buildPrompt(List<String> words, String difficulty, String style) {

        return """
                Create a short bilingual story using ALL of the following words:
                %s

                Difficulty: %s
                Style: %s

                Output format:
                [EN] English version
                [KO] Korean translation
                """.formatted(String.join(", ", words), difficulty, style);
    }

    /** ================================================
     *   텍스트 추출 함수
     * ================================================ */
    private String extract(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return "";

        s += start.length();
        int e = (end != null) ? text.indexOf(end, s) : text.length();

        if (e == -1) e = text.length();
        return text.substring(s, e).trim();
    }

    /** ================================================
     *   결과 DTO
     * ================================================ */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StoryResult {
        private boolean success;
        private String storyEn;
        private String storyKo;
        private List<String> usedWords;
    }
}
