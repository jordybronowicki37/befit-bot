package dev.jb.befit.backend.service;

import dev.jb.befit.backend.service.exceptions.ExerciseNotFoundException;
import dev.jb.befit.backend.service.exceptions.NoProgressMadeException;
import dev.jb.befit.backend.service.exceptions.NotEnoughProgressException;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressImageService {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private final ExerciseTypeService exerciseTypeService;
    private final ExerciseLogService logService;
    private final UserService userService;
    private final GoalService goalService;

    public File createPersonalProgressChart(Snowflake userId, String exerciseName) {
        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.getUserActiveGoal(user, exerciseName);
        var exerciseType = exerciseTypeService.getByName(exerciseName).orElseThrow(() -> new ExerciseNotFoundException(exerciseName));

        var allExerciseLogs = logService.getAllByUserAndExerciseName(user, exerciseName);
        if (allExerciseLogs.isEmpty()) throw new NoProgressMadeException(exerciseType);
        if (allExerciseLogs.size() == 1) throw new NotEnoughProgressException(exerciseType);

        var progressSeries = new TimeSeries("Your progress");
        for (var exerciseLog : allExerciseLogs) {
            progressSeries.add(getMillisecond(exerciseLog.getCreated()), exerciseLog.getAmount());
        }
        var dataset = new TimeSeriesCollection(progressSeries);

        if (goal.isPresent()) {
            var goalSeries = new TimeSeries("Your goal");
            var goalStartDate = allExerciseLogs.get(0).getCreated();
            var goalEndDate = allExerciseLogs.get(allExerciseLogs.size() - 1).getCreated();
            goalSeries.add(getMillisecond(goalStartDate), goal.get().getAmount());
            goalSeries.add(getMillisecond(goalEndDate), goal.get().getAmount());
            dataset.addSeries(goalSeries);
        }

        return createChart(dataset, String.format("Exercise: %s", exerciseType.getName()), exerciseType.getMeasurementType().getLongName());
    }

    public File createChart(TimeSeriesCollection dataset, String title, String xAxisLabel) {
        var chart = ChartFactory.createTimeSeriesChart(title, "Time", xAxisLabel, dataset);

        // Set styling
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

    private Millisecond getMillisecond(LocalDateTime dateTime) {
        return new Millisecond(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
