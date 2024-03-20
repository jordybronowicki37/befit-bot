package dev.jb.befit.backend.service;

import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressImageService {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private final ExerciseLogService logService;
    private final UserService userService;

    public File createProgressImage(Snowflake userId, String exerciseName) {
        var user = userService.getOrCreateDiscordUser(userId);
        var allExerciseLogs = logService.getAllByUserIdAndExerciseName(user, exerciseName);
        var exerciseType = allExerciseLogs.get(0).getExerciseType();

        var series = new TimeSeries("Your progress");
        for (var exerciseLog : allExerciseLogs) {
            var date = Date.from(exerciseLog.getCreated().atZone(ZoneId.systemDefault()).toInstant());
            series.add(new Millisecond(date), exerciseLog.getAmount());
        }

        var dataset = new TimeSeriesCollection(series);

        // Create the chart
        var chart = ChartFactory.createTimeSeriesChart(
                String.format("Exercise: %s", exerciseType.getName()),
                "Time",
                "KG",
                dataset
        );

        // Customize the chart (optional)
        chart.setBackgroundPaint(Color.white);
        chart.getTitle().setPaint(Color.black);

        // Set custom date format for the X-axis
        var dateAxis = (DateAxis) chart.getXYPlot().getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM"));

        // Create an image from the chart
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        var rectangle = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
        chart.draw(graphics, rectangle);

        // Save the image to file
        try {
            var imageFile = File.createTempFile("progress-chart", "png");
            ImageIO.write(image, "png", imageFile);
            return imageFile;
        } catch (IOException e) {
            log.error("Error during saving line chart", e);
            throw new RuntimeException(e);
        }
    }
}
