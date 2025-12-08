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
import com.wordweb.entity.WrongAnswerLog;
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

    /**
     * 스토리 생성 및 DB 저장
     * @return StoryResult with storyId
     */
    @Transactional
    public StoryResult generateAndSaveStory(List<Long> wrongWordIds, String difficulty, String style) {

        // 오답 로그에서 실제 단어 목록 추출 (FETCH JOIN으로 N+1 문제 해결)
        List<WrongAnswerLog> wrongLogs = wrongAnswerLogRepository.findAllByIdWithWord(wrongWordIds);
        List<String> words = wrongLogs.stream()
                .map(log -> log.getWord().getWord())
                .toList();

        // AI 스토리 생성
        StoryResult result = generateStory(words.toArray(new String[0]), difficulty, style);
        if (!result.isSuccess()) {
            return result;
        }

        // 현재 로그인 사용자
        String email = SecurityUtil.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // WrongAnswerStory 저장 (AI 생성 제목 사용)
        WrongAnswerStory story = wrongAnswerStoryRepository.save(
                WrongAnswerStory.create(
                        user,
                        result.getTitle(),  // AI가 생성한 제목 사용
                        result.getStoryEn(),
                        result.getStoryKo()
                )
        );

        // STORY_WORD_LIST 저장 (WORD_ID + WRONG_WORD_ID 둘 다 저장)
        // 이미 조회한 wrongLogs를 재사용 (추가 DB 조회 없음)
        for (WrongAnswerLog log : wrongLogs) {
            // IS_USED_IN_STORY = true 업데이트
            log.markUsedInStory();
            wrongAnswerLogRepository.save(log);

            Long wordId = log.getWord().getWordId();  // WORD_ID 추출 (이미 FETCH JOIN됨)
            StoryWordList mapping = StoryWordList.create(
                    story.getStoryId(),
                    wordId,        // WORD_ID (히스토리 보존)
                    log.getWrongWordId()    // WRONG_WORD_ID (오답 추적)
            );
            storyWordListRepository.save(mapping);
        }

        // 배치 flush (모든 변경사항을 한 번에 DB에 반영)
        wrongAnswerLogRepository.flush();

        // storyId를 결과에 포함
        return StoryResult.builder()
                .success(result.isSuccess())
                .title(result.getTitle())  // AI 생성 제목 포함
                .storyEn(result.getStoryEn())
                .storyKo(result.getStoryKo())
                .usedWords(result.getUsedWords())
                .storyId(story.getStoryId())
                .build();
    }


    /**
     * DeepSeek API를 통한 스토리 생성
     */
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

                JSONObject jsonObj = new JSONObject(responseJson);

                String rawContent = jsonObj
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                String title = extract(rawContent, "[TITLE]", "[EN]").trim();
                String storyEn = extract(rawContent, "[EN]", "[KO]").trim().replace("**", "");
                String storyKo = extract(rawContent, "[KO]", null).trim().replace("**", "");

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
                            .title(title.isEmpty() ? "AI Generated Story" : title)  // 제목이 없으면 기본값
                            .storyEn(storyEn)
                            .storyKo(storyKo)
                            .usedWords(usedWords)
                            .build();
                }

            } catch (Exception e) {
                // DeepSeek API 호출 실패 시 재시도
            }
        }

        return StoryResult.builder()
                .success(false)
                .title("AI 스토리 생성 실패")
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
                [TITLE] Story title (in English, creative and engaging)
                [EN] English version of the story
                [KO] Korean translation of the story
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
        private String title;  // AI 생성 제목
        private String storyEn;
        private String storyKo;
        private List<String> usedWords;
        private Long storyId;  // 생성된 스토리 ID
    }
}
