package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.UserRepository;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.dto.ExperienceLevelDetails;
import dev.jb.befit.backend.service.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserExperienceService {
    private static final double GROWTH_RATE = 1.3;
    private static final long STARTING_LEVEL_LIMIT = 200L;

    private final UserRepository userRepository;

    public void addExperience(long userId, long experience) {
        if (experience < 0) throw new IllegalArgumentException("Experience cannot be negative");
        var user = userRepository.findById(userId);
        if (user.isEmpty()) throw new UserNotFoundException(userId);
        addExperience(user.get(), experience);
    }

    public void addExperience(User user, long experience) {
        if (experience < 0) throw new IllegalArgumentException("Experience cannot be negative");
        user.setXp(user.getXp() + experience);
        userRepository.save(user);
    }

    public static ExperienceLevelDetails getLevelData(long xp) {
        var bottomLevelXp = 0L;
        var topLevelXp = STARTING_LEVEL_LIMIT;
        var level = 1L;

        while (xp >= topLevelXp) {
            level++;
            bottomLevelXp = topLevelXp;
            topLevelXp += (long) (Math.ceil(topLevelXp * GROWTH_RATE / 10) * 10);
        }

        var remainingLevelXp = topLevelXp - xp;
        var completedLevelXp = xp - bottomLevelXp;

        return new ExperienceLevelDetails(level, remainingLevelXp, completedLevelXp, bottomLevelXp, topLevelXp);
    }

    public static File getXpLevelPicture(long xp) {
        var levelData = getLevelData(xp);

        int progress = (int) ((float) levelData.xpCompletedInLevel() / (levelData.xpBottomLevel() + levelData.xpTopLevel()) * 100);
        if (progress < 0) progress = 0;
        if (progress > 99) progress = 99;

        int progressBarWidth = 300;
        int progressBarHeight = 20;
        int progressBarX = 2;
        int progressBarY = 2;
        int width = progressBarWidth + 2 * progressBarX;
        int height = progressBarHeight + 2 * progressBarY;
        int textSize = 15;
        int textY = 18;
        int progressBarLength = (progressBarWidth * progress) / 100;
        if (progressBarLength < progressBarHeight) progressBarLength = progressBarHeight;

        var bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background to transparent by clearing the image and switch back to normal drawing mode
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, width, height);
        graphics.setComposite(AlphaComposite.SrcOver);

        // Draw the background
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight, progressBarHeight, progressBarHeight);

        // Draw the filled part of the progress bar
        graphics.setColor(Color.GREEN);
        graphics.fillRoundRect(progressBarX, progressBarY, progressBarLength, progressBarHeight, progressBarHeight, progressBarHeight);

        // Draw the border of the progress bar
        var thickBorder = new BasicStroke(2);
        graphics.setStroke(thickBorder);
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight, progressBarHeight, progressBarHeight);

        // Add progress text
        var text = levelData.xpRemainingInLevel() + "xp";
        graphics.setFont(new Font("Arial", Font.BOLD, textSize));
        graphics.setColor(Color.BLACK);
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int textX = progressBarLength - textWidth;
        if (textX < progressBarX + 5) textX = progressBarX + 5;
        graphics.drawString(text, textX, textY);

        graphics.dispose();

        try {
            var outputFile = File.createTempFile("experience", "png");
            ImageIO.write(bufferedImage, "png", outputFile);
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
