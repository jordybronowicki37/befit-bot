package dev.jb.befit.backend.service.visuals;

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
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitImageService {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private final HabitService habitService;

    public File getHabitReportChart(User user, HabitTimeRange habitTimeRange, LocalDate startDate) {
        var habits = habitService.getHabitsByUserAndTimeRange(user, habitTimeRange);
        if (habits.isEmpty()) throw new NoHabitsFoundException();

        var firstHabitDate = startDate != null ? startDate : habits.stream().map(Habit::getCreated).min(LocalDateTime::compareTo).get().toLocalDate();
        var logs = habits.stream()
                .flatMap(h -> h.getHabitLogs().stream())
                .filter(l -> startDate == null || l.getLogDate().isAfter(startDate))
                .toList();
        var logsGrouped = logs.stream().collect(Collectors.groupingBy(HabitLog::getLogDate));

        // Completed habits dataset
        var habitsSeries = new TimeSeries("Completed habits");
        habitsSeries.add(getMillisecond(firstHabitDate), 0);
        for (var logGroup : logsGrouped.entrySet()) {
            habitsSeries.addOrUpdate(getMillisecond(logGroup.getKey()), logGroup.getValue().size());
        }
        var dataset = new TimeSeriesCollection();
        dataset.addSeries(habitsSeries);
        var barWidthDuration = switch (habitTimeRange) {
            case DAILY -> Duration.ofDays(1);
            case WEEKLY -> Duration.ofDays(7);
            case MONTHLY -> Duration.ofDays(30);
        };
        var barDataset = new XYBarDataset(dataset, (double) barWidthDuration.toMillis());

        // Max habits dataset
        var maxHabitsSeries = new TimeSeries("Max Possible Habits");
        for (var logGroup : logsGrouped.entrySet()) {
            var logDate = logGroup.getKey();
            maxHabitsSeries.addOrUpdate(getMillisecond(logDate), checkAmountOfHabitsActiveOnDate(logDate.atTime(19, 0), habits));
        }
        var maxDataset = new TimeSeriesCollection();
        maxDataset.addSeries(maxHabitsSeries);

        // Create bar chart
        var chart = ChartFactory.createXYBarChart(
                String.format("Overview of your completed %s habits", habitTimeRange.name().toLowerCase()),
                "Date",
                true,
                "Completed habits",
                barDataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Colors
        var colorBlack = new Color(18, 18, 20);
        var colorGrey = new Color(36, 36, 41);
        var neonCyan = new Color(0, 255, 255);
        var neonGreen = new Color(57, 255, 20);

        // Background & Title Styling
        chart.setBackgroundPaint(colorBlack);
        chart.getTitle().setPaint(Color.WHITE);

        // Legend Styling
        var legend = chart.getLegend();
        if (legend != null) {
            legend.setItemPaint(Color.WHITE);
            legend.setBackgroundPaint(colorBlack);
            legend.setFrame(new BlockBorder(colorBlack));
        }

        // Plot Styling
        var plot = chart.getXYPlot();
        plot.setBackgroundPaint(colorGrey);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);

        // Axis Styling
        var rangeAxis = new NumberAxis("Habits");
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelPaint(Color.WHITE);
        rangeAxis.setLabelPaint(Color.WHITE);
        plot.setRangeAxis(rangeAxis);
        applyDateAxis(plot, habitTimeRange);

        // Set Bar Renderer with Proper Bar Width
        var renderer = new XYBarRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setSeriesPaint(0, neonCyan);
        renderer.setShadowVisible(false);
        renderer.setMargin(0.1);
        plot.setRenderer(0, renderer);

        // Set Line Renderer (For Max Possible Habits)
        var lineRenderer = new SamplingXYLineRenderer() {
            @Override
            public LegendItem getLegendItem(int datasetIndex, int series) {
                LegendItem item = super.getLegendItem(datasetIndex, series);
                if (item != null) {
                    item.setLineStroke(new BasicStroke(3.0f)); // Thicker stroke in legend
                }
                return item;
            }
        };
        lineRenderer.setSeriesPaint(0, neonGreen);
        lineRenderer.setSeriesStroke(0, new BasicStroke(3.0f));
        plot.setDataset(1, maxDataset);
        plot.setRenderer(1, lineRenderer);

        // Create an image from the chart
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

    private void applyDateAxis(XYPlot plot, HabitTimeRange habitTimeRange) {
        var domainAxis = switch (habitTimeRange) {
            case DAILY -> new DateAxis("Date");
            case WEEKLY -> {
                var axis = new DateAxis("Week Number");
                axis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 7));
                yield axis;
            }
            case MONTHLY -> {
                var axis = new DateAxis("Month");
                axis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, 1));
                yield axis;
            }
        };

        domainAxis.setDateFormatOverride(switch (habitTimeRange) {
            case DAILY -> new SimpleDateFormat("dd-MM");
            case WEEKLY -> new SimpleDateFormat("'W'w");
            case MONTHLY -> new SimpleDateFormat("MMM");
        });

        domainAxis.setTickLabelPaint(Color.WHITE);
        domainAxis.setLabelPaint(Color.WHITE);
        plot.setDomainAxis(domainAxis);
    }

    private Millisecond getMillisecond(LocalDate dateTime) {
        return new Millisecond(Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private long checkAmountOfHabitsActiveOnDate(LocalDateTime date, List<Habit> habits) {
        return habits.stream().filter(h -> h.getCreated().isBefore(date)).count();
    }
}
