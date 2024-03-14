package dev.jb.befit.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MotivationalService {
    public String getRandomQuote() {
        return "You can do it!";
    }
}
