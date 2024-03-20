package dev.jb.befit.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jb.befit.backend.service.dto.QuoteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MotivationalService {
    private static final Random random = new Random();
    private static final String quotesFilePath = "quotes.json";
    private static final String positiveReinforcementFilePath = "positive-reinforcement.json";

    private final ResourceLoader resourceLoader;

    private <T> T readResource(String resourcePath) throws IOException {
        var resource = resourceLoader.getResource("classpath:" + resourcePath);
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    private <T> T getRandomItem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public List<QuoteDto> getAllQuotes() throws IOException {
        return readResource(quotesFilePath);
    }

    public QuoteDto getRandomQuote() {
        try {
            var messages = getAllQuotes();
            return getRandomItem(messages);
        } catch (IOException e) {
            log.error("Failed to retrieve quotes", e);
            return null;
        }
    }

    public List<String> getAllPositiveReinforcements() throws IOException {
        return readResource(positiveReinforcementFilePath);
    }

    public String getRandomPositiveReinforcement() {
        try {
            var messages = getAllPositiveReinforcements();
            return getRandomItem(messages);
        } catch (IOException e) {
            log.error("Failed to retrieve quotes", e);
            return null;
        }
    }
}
