package dev.jb.befit.backend.service.visuals;

import dev.jb.befit.backend.data.HabitLogRepository;
import dev.jb.befit.backend.data.models.Habit;
import dev.jb.befit.backend.data.models.HabitLog;
import dev.jb.befit.backend.data.models.HabitTimeRange;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.HabitService;
import dev.jb.befit.backend.service.exceptions.NoHabitsFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.entity.StandardEntityCollection;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitImageService {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private final HabitService habitService;
    private final HabitLogRepository habitLogRepository;

    public File getHabitReportChart(User user, HabitTimeRange habitTimeRange) {
        var habits = habitService.getHabitsByUserAndTimeRange(user, habitTimeRange);
        if (habits.isEmpty()) throw new NoHabitsFoundException();

        var firstHabitDate = habits.stream().map(Habit::getCreated).min(LocalDateTime::compareTo).get();
        var logs = habitLogRepository.findAllByUser(user);
        var logsGrouped = logs.stream().collect(Collectors.groupingBy(HabitLog::getLogDate));

        var habitsSeries = new TimeSeries("Completed habits");
        habitsSeries.add(getMillisecond(firstHabitDate.toLocalDate()), 0);
        for (var logGroup : logsGrouped.entrySet()) {
            habitsSeries.addOrUpdate(getMillisecond(logGroup.getKey()), logGroup.getValue().size());
        }

        var dataset = new TimeSeriesCollection();
        dataset.addSeries(habitsSeries);

        // Create chart and style it
        var chart = ChartFactory.createTimeSeriesChart("Completed habits", "Time", "Habits", dataset, true, true, false);
        chart.setBackgroundPaint(Color.white);
        chart.getTitle().setPaint(Color.black);

        // Customize axis
        var plot = chart.getXYPlot();
        var domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM"));

        // Create an image from the chart
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        var chartArea = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
        var chartRenderingInfo = new ChartRenderingInfo(new StandardEntityCollection());
        chart.draw(graphics, chartArea, chartRenderingInfo);

        // Save the image to file
        try {
            var imageFile = File.createTempFile("habits-chart", "png");
            ImageIO.write(image, "png", imageFile);
            return imageFile;
        } catch (IOException e) {
            log.error("Error during saving habits bar chart", e);
            throw new RuntimeException(e);
        }
    }

    private Millisecond getMillisecond(LocalDate dateTime) {
        return new Millisecond(Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
