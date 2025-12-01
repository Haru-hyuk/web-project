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

import com.wordweb.entity.StoryWordList;
import com.wordweb.entity.User;
import com.wordweb.entity.WrongAnswerStory;
import com.wordweb.repository.StoryWordListRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WrongAnswerLogRepository;
import com.wordweb.repository.WrongAnswerStoryRepository;
import com.wordweb.security.SecurityUtil;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AIStoryService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";

    private final WrongAnswerStoryRepository wrongAnswerStoryRepository;
    private final StoryWordListRepository storyWordListRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final UserRepository userRepository;

    /** ================================================
     *   전체 프로세스: 스토리 생성 → DB 저장까지 처리
     * ================================================ */
    @Transactional
    public StoryResult generateAndSaveStory(List<Long> wrongWordIds, String difficulty, String style) {

        // 1) 오답 로그에서 실제 단어 목록 추출
        List<String> words = wrongAnswerLogRepository.findAllById(wrongWordIds)
                .stream()
                .map(log -> log.getWord().getWord()) // Word 엔티티 안의 텍스트
                .toList();

        // 2) AI 스토리 생성
        StoryResult result = generateStory(words.toArray(new String[0]), difficulty, style);

        if (!result.isSuccess()) {
            return result;
        }

        // 3) 현재 로그인 사용자
        String email = SecurityUtil.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4) WrongAnswerStory 저장
        WrongAnswerStory story = wrongAnswerStoryRepository.save(
                WrongAnswerStory.create(
                        user,
                        "AI 자동 생성 스토리",
                        result.getStoryEn(),
                        result.getStoryKo()
                )
        );

        // 5) STORY_WORD_LIST 저장
        for (Long wrongWordId : wrongWordIds) {
            if (wrongAnswerLogRepository.existsById(wrongWordId)) {
                StoryWordList mapping = StoryWordList.create(
                        story.getStoryId(),
                        wrongWordId
                );
                storyWordListRepository.save(mapping);
            }
        }

        return result;
    }


    /** ==========================================================
     *   DeepSeek 주고받는 원래 스토리 생성 기능 — 그대로 유지
     * ========================================================== */
    public StoryResult generateStory(String[] words, String difficulty, String style) {

        String prompt = buildPrompt(Arrays.asList(words), difficulty, style);

        int maxAttempts = 3;
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;

            try {
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

                System.out.println("\n================ RAW DEEPSEEK RESPONSE ================");
                System.out.println(responseJson);
                System.out.println("=======================================================\n");

                JSONObject jsonObj = new JSONObject(responseJson);

                String rawContent = jsonObj
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                String storyEn = extract(rawContent, "[EN]", "[KO]").trim();
                String storyKo = extract(rawContent, "[KO]", null).trim();

                List<String> usedWords = new ArrayList<>();
                String storyLower = storyEn.toLowerCase();

                boolean allUsed = true;
                for (String w : words) {
                    if (storyLower.contains(w.toLowerCase())) {
                        usedWords.add(w);
                    } else {
                        allUsed = false;
                    }
                }

                if (allUsed) {
                    return StoryResult.builder()
                            .success(true)
                            .storyEn(storyEn)
                            .storyKo(storyKo)
                            .usedWords(usedWords)
                            .build();
                }

            } catch (Exception e) {
                System.out.println("❌ DeepSeek 에러: " + e.getMessage());
            }
        }

        return StoryResult.builder()
                .success(false)
                .storyEn("AI 스토리 생성 실패")
                .storyKo("AI 스토리 생성 실패")
                .usedWords(List.of())
                .build();
    }


    /** ================================================ */
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

    private String extract(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return "";
        s += start.length();

        int e = (end != null) ? text.indexOf(end, s) : text.length();
        if (e == -1) e = text.length();

        return text.substring(s, e).trim();
    }

    /** 결과 DTO */
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
