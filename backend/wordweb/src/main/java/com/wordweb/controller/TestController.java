package com.wordweb.controller;

import com.wordweb.repository.UserRepository;
import com.wordweb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    @GetMapping("/api/test/server")
    public String serverCheck() {
        return "Server OK";
    }

    @GetMapping("/api/test/db")
    public String dbCheck() {
        long userCount = userRepository.count();
        long wordCount = wordRepository.count();

        return "DB OK / Users: " + userCount + " / Words: " + wordCount;
    }
}
