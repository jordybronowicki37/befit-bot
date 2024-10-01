package dev.jb.befit.backend.discord.registration;

import dev.jb.befit.backend.data.models.AchievementIcon;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.util.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmojiRegistrarService {
    private static final String emojiFilesMatcher = "classpath:achievement-icons/*.png";

    @Value("${discord.guilds.management}")
    private Long managementGuildId;

    private final GatewayDiscordClient discordClient;
    private final ResourceLoader resourceLoader;

    private final Map<AchievementIcon, Snowflake> achievementEmojiIds = new HashMap<>();
    private final Map<AchievementIcon, Snowflake> lockedAchievementEmojiIds = new HashMap<>();

    public void registerEmojis() throws IOException {
        var managementGuild = discordClient.getGuildById(Snowflake.of(managementGuildId)).timeout(Duration.ofSeconds(5)).block();
        if (managementGuild == null) throw new NullPointerException("Failed to get management guild");

        var existingEmojis = managementGuild.getEmojis().collectList().timeout(Duration.ofSeconds(5)).block();
        if (existingEmojis == null) throw new NullPointerException("Failed to get existing emojis of management guild");

        achievementEmojiIds.clear();
        lockedAchievementEmojiIds.clear();

        var emojiFiles = getAllEmojiFiles();
        for (var file : emojiFiles) {
            var achievement = Arrays.stream(AchievementIcon.values()).filter(a -> a.getIconFileName().equals(file.getName())).findFirst();
            if (achievement.isEmpty()) continue;
            var emojiName = achievement.get().getDisplayName();
            var lockedEmojiName = emojiName + "_locked";

            var foundEmoji = existingEmojis.stream().filter(e -> e.getName().equals(emojiName)).findFirst();
            Snowflake emojiId;
            if (foundEmoji.isEmpty()) {
                var achievementImage = Image.ofRaw(Files.readAllBytes(file.toPath()), Image.Format.PNG);
                log.info("Adding achievement emoji with name: {}", emojiName);
                var newEmoji = managementGuild.createEmoji(emojiName, achievementImage).timeout(Duration.ofSeconds(5)).block();
                assert newEmoji != null;
                emojiId = newEmoji.getId();
            } else {
                emojiId = foundEmoji.get().getId();
            }
            achievementEmojiIds.put(achievement.get(), emojiId);

            var foundLockedEmoji = existingEmojis.stream().filter(e -> e.getName().equals(lockedEmojiName)).findFirst();
            Snowflake lockedEmojiId;
            if (foundLockedEmoji.isEmpty()) {
                var lockedAchievementFile = grayScaleImage(file);
                var lockedAchievementImage = Image.ofRaw(Files.readAllBytes(lockedAchievementFile.toPath()), Image.Format.PNG);
                log.info("Adding locked achievement emoji with name: {}", lockedEmojiName);
                var newEmoji = managementGuild.createEmoji(lockedEmojiName, lockedAchievementImage).timeout(Duration.ofSeconds(5)).block();
                assert newEmoji != null;
                lockedEmojiId = newEmoji.getId();
            } else {
                lockedEmojiId = foundLockedEmoji.get().getId();
            }
            lockedAchievementEmojiIds.put(achievement.get(), lockedEmojiId);
        }
    }

    public List<File> getAllEmojiFiles() throws IOException {
        var resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(emojiFilesMatcher);
        return Arrays.stream(resources)
                .map(r -> {
                    try {
                        return r.getFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public void removeAllEmojis() {
        var managementGuild = discordClient.getGuildById(Snowflake.of(managementGuildId)).timeout(Duration.ofSeconds(5)).block();
        if (managementGuild == null) throw new NullPointerException("Failed to get management guild");

        var existingEmojis = managementGuild.getEmojis().collectList().timeout(Duration.ofSeconds(5)).block();
        assert existingEmojis != null;
        existingEmojis.forEach(e -> e.delete().timeout(Duration.ofSeconds(5)).block());
        log.info("Removed all emojis from management guild");
    }

    public Snowflake getEmojiId(AchievementIcon achievementIcon, boolean locked) {
        if (locked) return lockedAchievementEmojiIds.get(achievementIcon);
        return achievementEmojiIds.get(achievementIcon);
    }

    private File grayScaleImage(File file) throws IOException {
        var image = ImageIO.read(file);
        var grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // Apply grayscale filter to graphics
        var g2d = grayscaleImage.createGraphics();
        var op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(image, grayscaleImage);
        g2d.dispose();

        // Save the grayscaled image
        var fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        var output = File.createTempFile(fileName + "_gray", ".png");
        ImageIO.write(grayscaleImage, "png", output);
        return output;
    }
}
