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
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MotivationalService {
    private final ResourceLoader resourceLoader;
    private static final Random random = new Random();

    private static final String filePath = "quotes.json";

    public List<QuoteDto> getAllQuotes() throws IOException {
        var resource = resourceLoader.getResource("classpath:" + filePath);
        var objectMapper = new ObjectMapper();
        var jsonData = objectMapper.readValue(resource.getInputStream(), new TypeReference<List<Map<String, String>>>() {});

        // Convert the list of maps into a list of Message objects
        return jsonData.stream()
                .map(map -> new QuoteDto(map.get("message"), map.get("author")))
                .toList();
    }

    public QuoteDto getRandomQuote() {
        try {
            var messages = getAllQuotes();
            return messages.get(random.nextInt(messages.size()));
        } catch (IOException e) {
            log.error("Failed to retrieve quotes", e);
            return null;
        }
    }
}
