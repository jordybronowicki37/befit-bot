package dev.jb.befit.backend.discord.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandRegistrarRunner implements CommandLineRunner {
    private final CommandRegistrarService commandRegistrarService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Started registering all guild commands");
        commandRegistrarService.registerAllCommands();
        log.info("Finished registering all guild commands");
    }
}
