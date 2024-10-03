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
            topLevelXp = (long) (Math.ceil(topLevelXp * GROWTH_RATE / 10) * 10);
        }

        var remainingLevelXp = topLevelXp - xp;
        var completedLevelXp = xp - bottomLevelXp;

        return new ExperienceLevelDetails(level, remainingLevelXp, completedLevelXp, bottomLevelXp, topLevelXp);
    }

    public static File getXpLevelPicture(long xp) {
        var levelData = getLevelData(xp);

        int progress = (int) ((float) levelData.xpCompletedInLevel() / (levelData.xpTopLevel() - levelData.xpBottomLevel()) * 100);
        if (progress < 0) progress = 0;
        if (progress > 99) progress = 99;

        int barWidth = 300;
        int progressBarHeight = 20;
        int cornerArc = progressBarHeight;
        int imageMargin = 2;
        int width = barWidth + 2*imageMargin;
        int height = progressBarHeight + 2*imageMargin;
        int textSize = 15;
        int textY = 18;
        var progressText = xp + "XP";
        var levelText = "LVL " + levelData.level();

        var bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke thickBorder = new BasicStroke(2);
        graphics.setStroke(thickBorder);
        graphics.setFont(new Font("Arial", Font.BOLD, textSize));
        int levelTextWidth = graphics.getFontMetrics().stringWidth(levelText);
        int levelBarWidth = levelTextWidth+6+progressBarHeight/2;
        int progressBarX = levelTextWidth+imageMargin;
        int progressBarLength = (barWidth - levelBarWidth + cornerArc/2 + imageMargin + 1) * progress / 100;
        if (progressBarLength < progressBarHeight) progressBarLength = progressBarHeight;

        // Set the background to transparent by clearing the image and switch back to normal drawing mode
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, width, height);
        graphics.setComposite(AlphaComposite.SrcOver);

        // Draw the progress bar
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(imageMargin, imageMargin, barWidth-1, progressBarHeight, cornerArc, cornerArc);
        graphics.setColor(Color.GREEN);
        graphics.fillRoundRect(progressBarX, imageMargin, progressBarLength, progressBarHeight, cornerArc, cornerArc);

        // Draw the border of the progress bar
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(imageMargin, imageMargin, barWidth-imageMargin, progressBarHeight, cornerArc, cornerArc);

        // Add progress text
        int progressTextWidth = graphics.getFontMetrics().stringWidth(progressText);
        int progressTextX = levelTextWidth + progressBarLength - progressTextWidth;
        if (progressTextX < progressBarX + cornerArc) progressTextX = progressBarX + cornerArc;
        graphics.setColor(Color.BLACK);
        graphics.drawString(progressText, progressTextX, textY);

        // Draw the level background
        graphics.setColor(Color.CYAN);
        graphics.fillRoundRect(imageMargin, imageMargin, levelBarWidth, progressBarHeight, cornerArc, cornerArc);

        // Draw the level border
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(imageMargin, imageMargin, levelBarWidth, progressBarHeight, cornerArc, cornerArc);

        // Add level text
        graphics.setColor(Color.BLACK);
        graphics.drawString(levelText, 10, textY);

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
