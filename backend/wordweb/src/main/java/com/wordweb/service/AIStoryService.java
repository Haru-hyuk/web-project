package com.wordweb.service;

import com.wordweb.dto.ai.AIStoryResult;
import com.wordweb.entity.*;
import com.wordweb.repository.*;
import com.wordweb.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, Boolean> generatingLocks = new ConcurrentHashMap<>();

    /* ===========================
       스토리 생성 + 저장 (최종)
       =========================== */
    @Transactional
    public AIStoryResult generateAndSaveStory(List<Long> wrongWordIds) {

        User user = userRepository.findByEmail(SecurityUtil.getLoginUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String lockKey = user.getUserId() + "_" +
                wrongWordIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));

        if (generatingLocks.putIfAbsent(lockKey, true) != null) {
            return AIStoryResult.builder()
                    .success(false)
                    .title("Generating...")
                    .storyEn("이미 생성 중입니다.")
                    .storyKo("이미 생성 중입니다.")
                    .usedWords(List.of())
                    .build();
        }

        try {
            List<WrongAnswerLog> logs =
                    wrongAnswerLogRepository.findAllByIdWithWord(wrongWordIds)
                            .stream()
                            .filter(l -> !Boolean.TRUE.equals(l.getIsUsedInStory()))
                            .toList();

            List<String> words = logs.stream()
                    .map(l -> l.getWord().getWord())
                    .distinct()
                    .toList();

            AIStoryResult result = generateStory(words.toArray(new String[0]));
            if (!result.isSuccess()) return result;

            WrongAnswerStory story = wrongAnswerStoryRepository.save(
                    WrongAnswerStory.create(
                            user,
                            result.getTitle(),   // 영어 제목
                            result.getTitleKo(),   // ⭐ 한글 제목
                            result.getStoryEn(),
                            result.getStoryKo()
                    )
            );

            for (WrongAnswerLog log : logs) {
                log.markUsedInStory();
                storyWordListRepository.save(
                        StoryWordList.create(
                                story.getStoryId(),
                                log.getWord().getWordId(),
                                log.getWrongWordId()
                        )
                );
            }

            return AIStoryResult.builder()
                    .success(true)
                    .storyId(story.getStoryId())
                    .title(result.getTitle())
                    .titleKo(result.getTitleKo())
                    .storyEn(result.getStoryEn())
                    .storyKo(result.getStoryKo())
                    .usedWords(result.getUsedWords())
                    .build();

        } finally {
            generatingLocks.remove(lockKey);
        }
    }

    /* ===========================
       AI 호출 (제목/본문 동시 생성)
       =========================== */
    public AIStoryResult generateStory(String[] words) {

        String prompt = """
            Create a bilingual story using ALL words: %s

            Rules:
            - The English title must be natural and concise.
            - The Korean title must be a natural translation of the English title.

            Format (STRICT):
            [TITLE_EN]
            English title

            [TITLE_KO]
            Korean title

            [EN]
            English story

            [KO]
            Korean translation
            """.formatted(String.join(", ", words));

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build();

            JSONObject body = new JSONObject()
                    .put("model", "deepseek-chat")
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt)));

            Request request = new Request.Builder()
                    .url(DEEPSEEK_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(RequestBody.create(
                            MediaType.parse("application/json"),
                            body.toString()))
                    .build();

            Response response = client.newCall(request).execute();
            String content = new JSONObject(response.body().string())
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return AIStoryResult.builder()
                    .success(true)
                    .title(extract(content, "[TITLE_EN]", "[TITLE_KO]"))
                    .titleKo(extract(content, "[TITLE_KO]", "[EN]"))
                    .storyEn(extract(content, "[EN]", "[KO]"))
                    .storyKo(extract(content, "[KO]", null))
                    .usedWords(Arrays.asList(words))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return AIStoryResult.builder().success(false).build();
        }
    }

    private String extract(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return "";
        s += start.length();
        int e = end != null ? text.indexOf(end, s) : text.length();
        return text.substring(s, e == -1 ? text.length() : e).trim();
    }
}
