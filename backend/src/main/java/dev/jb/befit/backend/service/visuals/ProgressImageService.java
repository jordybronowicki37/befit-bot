package dev.jb.befit.backend.service.visuals;

import dev.jb.befit.backend.data.models.*;
import dev.jb.befit.backend.service.ExerciseLogService;
import dev.jb.befit.backend.service.ExerciseTypeService;
import dev.jb.befit.backend.service.GoalService;
import dev.jb.befit.backend.service.UserService;
import dev.jb.befit.backend.service.exceptions.NoProgressMadeException;
import dev.jb.befit.backend.service.exceptions.NotEnoughProgressException;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressImageService {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final int AVATAR_SIZE = 30;

    private final ExerciseTypeService exerciseTypeService;
    private final ExerciseLogService logService;
    private final UserService userService;
    private final GoalService goalService;
    private final GatewayDiscordClient discordClient;

    public File createPersonalProgressChart(Snowflake userId, String exerciseName) {
        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.getActiveUserGoal(user, exerciseName);
        var exerciseType = exerciseTypeService.findByName(exerciseName);

        var allExerciseLogs = logService.getAllByUserAndExerciseName(user, exerciseName);
        if (allExerciseLogs.isEmpty()) throw new NoProgressMadeException(exerciseType);
        if (allExerciseLogs.size() == 1) throw new NotEnoughProgressException(exerciseType);
        var sortedLogs = allExerciseLogs.stream().sorted(Comparator.comparing(ExerciseLog::getCreated)).toList();

        Map<User, TimeSeries> userTimeSeriesMap = new HashMap<>();

        // Calculate the progress
        var progressSeries = new TimeSeries("Your progress");
        for (var exerciseLog : sortedLogs) {
            progressSeries.add(getMillisecond(exerciseLog.getCreated()), exerciseLog.getAmount());
        }
        userTimeSeriesMap.put(user, progressSeries);

        // Add goal line if a goal is set
        goal.ifPresent(value -> userTimeSeriesMap.put(null, getGoalTimeSeries(value, sortedLogs)));

        return createChart(userTimeSeriesMap, String.format("Exercise: %s", exerciseType.getName()), exerciseType.getMeasurementType().getLongName());
    }

    public File createGlobalProgressChart(Snowflake userId, String exerciseName) {
        var user = userService.getOrCreateDiscordUser(userId);
        var goal = goalService.getActiveUserGoal(user, exerciseName);
        var exerciseType = exerciseTypeService.findByName(exerciseName);

        var allExerciseLogs = logService.getAllByExerciseName(exerciseName);
        if (allExerciseLogs.isEmpty()) throw new NoProgressMadeException(exerciseType);
        if (allExerciseLogs.size() == 1) throw new NotEnoughProgressException(exerciseType);
        var groupedLogs = allExerciseLogs.stream().collect(Collectors.groupingBy(ExerciseLog::getUser));

        // Calculate progress for each user
        Map<User, TimeSeries> userTimeSeriesMap = new HashMap<>();
        for (var userExerciseLogGroup : groupedLogs.entrySet()) {
            var userExerciseLogs = userExerciseLogGroup.getValue().stream().sorted(Comparator.comparing(ExerciseLog::getCreated)).toList();
            if (userExerciseLogs.size() <= 1) continue;
            var progressSeries = new TimeSeries(getUsername(userExerciseLogGroup.getKey()));
            for (var exerciseLog : userExerciseLogs) {
                progressSeries.add(getMillisecond(exerciseLog.getCreated()), exerciseLog.getAmount());
            }
            userTimeSeriesMap.put(userExerciseLogGroup.getKey(), progressSeries);
        }

        if (userTimeSeriesMap.isEmpty()) throw new NoProgressMadeException(exerciseType);

        // Add goal line if a goal is set
        goal.ifPresent(value -> userTimeSeriesMap.put(null, getGoalTimeSeries(value, allExerciseLogs)));

        return createChart(userTimeSeriesMap, String.format("Exercise: %s", exerciseType.getName()), exerciseType.getMeasurementType().getLongName());
    }

    private TimeSeries getGoalTimeSeries(Goal goal, List<ExerciseLog> allExerciseLogs) {
        var goalSeries = new TimeSeries("Your goal");
        var goalStartDate = allExerciseLogs.get(0).getCreated();
        var goalEndDate = allExerciseLogs.get(allExerciseLogs.size() - 1).getCreated();
        goalSeries.add(getMillisecond(goalStartDate), goal.getAmount());
        goalSeries.add(getMillisecond(goalEndDate), goal.getAmount());
        return goalSeries;
    }

    public File createChart(Map<User, TimeSeries> userTimeSeriesMap, String title, String xAxisLabel) {
        var dataset = new TimeSeriesCollection();
        userTimeSeriesMap.values().forEach(dataset::addSeries);

        // Create a chart and style it
        var chart = ChartFactory.createTimeSeriesChart(title, "Time", xAxisLabel, dataset, true, true, false);
        chart.setPadding(new RectangleInsets(0, 0, 0, 20));
        chart.setBackgroundPaint(colorBlack);
        chart.getTitle().setPaint(Color.WHITE);

        // Style the legend
        var legend = chart.getLegend();
        if (legend != null) {
            legend.setItemPaint(Color.WHITE);
            legend.setBackgroundPaint(colorBlack);
            legend.setFrame(new BlockBorder(colorBlack));
        }

        // Customize plot
        var plot = chart.getXYPlot();
        plot.setBackgroundPaint(colorGrey);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);

        // Axis styling
        var domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM"));
        domainAxis.setLabelPaint(Color.WHITE);
        domainAxis.setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

        // Customize series renderers
        var renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < userTimeSeriesMap.size(); i++) {
            int colorIndex = i % neonColors.length;
            renderer.setSeriesPaint(i, neonColors[colorIndex]);
            renderer.setSeriesStroke(i, new BasicStroke(2.5f)); // Thicker lines
            renderer.setSeriesShapesVisible(i, true); // Show data points
            renderer.setSeriesShape(i, new Ellipse2D.Double(-3, -3, 6, 6)); // Circle
        }
        plot.setRenderer(renderer);

        // Draw chart to image
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var chartArea = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
        var chartRenderingInfo = new ChartRenderingInfo(new StandardEntityCollection());
        chart.draw(graphics, chartArea, chartRenderingInfo);

        // Add user avatars
        userTimeSeriesMap.entrySet().forEach(v -> insertUserAvatar(chart, chartRenderingInfo, graphics, v));

        // Save image
        try {
            var imageFile = File.createTempFile("progress-chart", "png");
            ImageIO.write(image, "png", imageFile);
            return imageFile;
        } catch (IOException e) {
            log.error("Error during saving progress line chart", e);
            throw new RuntimeException(e);
        }
    }

    private void insertUserAvatar(JFreeChart chart, ChartRenderingInfo chartRenderingInfo, Graphics2D graphics, Map.Entry<User, TimeSeries> userTimeSeriesMap) {
        var user = userTimeSeriesMap.getKey();
        var timeSeries = userTimeSeriesMap.getValue();
        if (user == null) return;
        if (!(user instanceof DiscordUser discordUser)) return;

        var plotInfo = chartRenderingInfo.getPlotInfo();
        var plotArea = plotInfo.getDataArea();
        var plot = chart.getXYPlot();
        var rangeAxis = plot.getRangeAxis();
        var domainAxis = (DateAxis) plot.getDomainAxis();

        try {
            var avatar = createCircularImage(getAvatar(discordUser));

            // Convert the time and value of last item into x and y Java2D coordinates
            var lastItem = timeSeries.getDataItem(timeSeries.getItemCount() - 1);
            var x = domainAxis.valueToJava2D(lastItem.getPeriod().getFirstMillisecond(), plotArea, plot.getDomainAxisEdge());
            var y = rangeAxis.valueToJava2D(lastItem.getValue().doubleValue(), plotArea, plot.getRangeAxisEdge());
            var actualX = (int) x + (int) (AVATAR_SIZE * 0.2);
            var actualY = (int) y - (AVATAR_SIZE / 2);

            graphics.drawImage(avatar, actualX, actualY, AVATAR_SIZE, AVATAR_SIZE, null);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private BufferedImage getAvatar(DiscordUser discordUser) throws IOException {
        var discordUserAcc = discordClient.getUserById(discordUser.getDiscordId()).block();
        if (discordUserAcc == null) throw new NullPointerException("Discord user was not found with id: " + discordUser.getDiscordId());
        var discordAvatar = discordUserAcc.getAvatar().block();
        if (discordAvatar == null) throw new NullPointerException("Discord user avatar was not found with id: " + discordUser.getDiscordId());
        return ImageIO.read(new ByteArrayInputStream(discordAvatar.getData()));
    }

    private BufferedImage createCircularImage(BufferedImage image) {
        var circularImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var circularGraphics = circularImage.createGraphics();
        circularGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        circularGraphics.setClip(new Ellipse2D.Float(0, 0, image.getWidth(), image.getHeight()));
        circularGraphics.drawImage(image, 0, 0, null);
        circularGraphics.dispose();
        return circularImage;
    }

    private String getUsername(User user) {
        if (user instanceof DiscordUser discordUser) {
            var actualDiscordUser = discordClient.getUserById(discordUser.getDiscordId()).block();
            if (actualDiscordUser == null) {
                log.error("Discord user not found with id: {}", discordUser.getDiscordId());
                return "Unknown discord user";
            }
            return actualDiscordUser.getUsername();
        }
        if (user instanceof WebUser webUser) {
            return webUser.getUsername();
        }
        return "Unknown user";
    }

    private Millisecond getMillisecond(LocalDateTime dateTime) {
        return new Millisecond(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    // Bright neon colors list
    private final Color[] neonColors = new Color[]{
            new Color(57, 255, 20),   // Neon green
            new Color(0, 255, 255),   // Neon cyan
            new Color(255, 0, 255),   // Neon magenta
            new Color(255, 255, 0),   // Neon yellow
            new Color(255, 105, 180), // Hot pink
            new Color(0, 255, 127),   // Spring green
            new Color(0, 191, 255),   // Deep sky blue
            new Color(255, 69, 0)     // Neon red-orange
    };
    private final Color colorBlack = new Color(18, 18, 20);
    private final Color colorGrey = new Color(36, 36, 41);
}
