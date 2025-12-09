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
import com.wordweb.entity.Word;
import com.wordweb.entity.WrongAnswerLog;
import com.wordweb.entity.WrongAnswerStory;
import com.wordweb.repository.StoryWordListRepository;
import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import com.wordweb.repository.WrongAnswerLogRepository;
import com.wordweb.repository.WrongAnswerStoryRepository;
import com.wordweb.security.SecurityUtil;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIStoryService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";

    private final WrongAnswerStoryRepository wrongAnswerStoryRepository;
    private final StoryWordListRepository storyWordListRepository;
    private final WrongAnswerLogRepository wrongAnswerLogRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, Boolean> generatingLocks = new ConcurrentHashMap<>();

    @Transactional
    public StoryResult generateAndSaveStory(List<Long> wrongWordIds) {
        String email = SecurityUtil.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String sortedIds = wrongWordIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String lockKey = user.getUserId() + "_" + sortedIds;

        if (generatingLocks.putIfAbsent(lockKey, Boolean.TRUE) != null) {
            return StoryResult.builder()
                    .success(false)
                    .title("스토리 생성 중")
                    .storyEn("이미 스토리를 생성하고 있습니다.")
                    .storyKo("이미 스토리를 생성하고 있습니다.")
                    .usedWords(List.of())
                    .build();
        }

        try {
            // 단순화: 한 번에 조회하고 필터링
            List<WrongAnswerLog> wrongLogs = wrongAnswerLogRepository.findAllByIdWithWord(wrongWordIds)
                    .stream()
                    .filter(log -> log != null)
                    .filter(log -> !Boolean.TRUE.equals(log.getIsUsedInStory()))
                    .filter(log -> log.getWord() != null)
                    .filter(log -> log.getWord().getWord() != null)
                    .filter(log -> !log.getWord().getWord().trim().isEmpty())
                    .collect(Collectors.toList());

            if (wrongLogs.isEmpty()) {
                return StoryResult.builder()
                        .success(false)
                        .title("유효한 단어를 찾을 수 없습니다")
                        .storyEn("선택한 단어 중 유효한 단어를 찾을 수 없습니다.")
                        .storyKo("선택한 단어 중 유효한 단어를 찾을 수 없습니다.")
                        .usedWords(List.of())
                        .build();
            }

            List<String> words = wrongLogs.stream()
                    .map(log -> log.getWord().getWord().trim())
                    .filter(word -> !word.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (words.isEmpty()) {
                return StoryResult.builder()
                        .success(false)
                        .title("단어 목록이 비어있습니다")
                        .storyEn("스토리 생성에 사용할 단어가 없습니다.")
                        .storyKo("스토리 생성에 사용할 단어가 없습니다.")
                        .usedWords(List.of())
                        .build();
            }

            StoryResult result = generateStory(words.toArray(new String[0]));
            if (!result.isSuccess()) {
                return result;
            }

            WrongAnswerStory story = wrongAnswerStoryRepository.save(
                    WrongAnswerStory.create(
                            user,
                            result.getTitle(),
                            result.getStoryEn(),
                            result.getStoryKo()
                    )
            );

            List<WrongAnswerLog> logsToUpdate = new ArrayList<>();
            List<StoryWordList> mappingsToSave = new ArrayList<>();
            
            for (WrongAnswerLog log : wrongLogs) {
                if (log == null || log.getWord() == null || log.getWord().getWordId() == null) {
                    continue;
                }

                log.markUsedInStory();
                logsToUpdate.add(log);

                Long wordId = log.getWord().getWordId();
                StoryWordList mapping = StoryWordList.create(
                        story.getStoryId(),
                        wordId,
                        log.getWrongWordId()
                );
                mappingsToSave.add(mapping);
            }

            if (!logsToUpdate.isEmpty()) {
                wrongAnswerLogRepository.saveAll(logsToUpdate);
            }
            if (!mappingsToSave.isEmpty()) {
                storyWordListRepository.saveAll(mappingsToSave);
            }

            wrongAnswerLogRepository.flush();
            storyWordListRepository.flush();

            return StoryResult.builder()
                    .success(result.isSuccess())
                    .title(result.getTitle())
                    .storyEn(result.getStoryEn())
                    .storyKo(result.getStoryKo())
                    .usedWords(result.getUsedWords())
                    .storyId(story.getStoryId())
                    .build();
        } finally {
            generatingLocks.remove(lockKey);
        }
    }


    public StoryResult generateStory(String[] words) {

        String prompt = buildPrompt(Arrays.asList(words));

        int maxAttempts = 3;
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;

            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(45, TimeUnit.SECONDS)
                        .callTimeout(60, TimeUnit.SECONDS)
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

                return StoryResult.builder()
                        .success(true)
                        .title(title.isEmpty() ? "AI Generated Story" : title)
                        .storyEn(storyEn)
                        .storyKo(storyKo)
                        .usedWords(Arrays.asList(words))
                        .build();

            } catch (Exception e) {
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


    private String buildPrompt(List<String> words) {
        return """
                Create a bilingual story using ALL words: %s
                Format:
                [TITLE]Title
                [EN]English story
                [KO]Korean translation
                """.formatted(String.join(", ", words));
    }

    private String extract(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return "";
        s += start.length();

        int e = (end != null) ? text.indexOf(end, s) : text.length();
        if (e == -1) e = text.length();

        return text.substring(s, e).trim();
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StoryResult {
        private boolean success;
        private String title;
        private String storyEn;
        private String storyKo;
        private List<String> usedWords;
        private Long storyId;
    }
}
