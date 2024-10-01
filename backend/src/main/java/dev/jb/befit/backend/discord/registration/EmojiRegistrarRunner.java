package dev.jb.befit.backend.discord.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmojiRegistrarRunner implements CommandLineRunner {
    private final EmojiRegistrarService emojiRegistrarService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Started registering all custom emojis");
        emojiRegistrarService.registerEmojis();
        log.info("Finished registering all custom emojis");
    }
}
