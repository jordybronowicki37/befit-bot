package dev.jb.befit.backend.discord.commands.handlers;

import dev.jb.befit.backend.data.models.ExerciseRecord;
import dev.jb.befit.backend.data.models.ExerciseType;
import dev.jb.befit.backend.data.models.GoalDirection;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.discord.commands.CommandConstants;
import dev.jb.befit.backend.discord.commands.CommandHandlerHelper;
import dev.jb.befit.backend.discord.listeners.DiscordChatInputInteractionEventListener;
import dev.jb.befit.backend.service.ExerciseRecordService;
import dev.jb.befit.backend.service.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardCommandHandler extends DiscordChatInputInteractionEventListener {
    private final UserService userService;
    private final ExerciseRecordService exerciseRecordService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @Override
    public String getCommandNameFilter() {
        return CommandConstants.CommandLeaderboard;
    }

    @Override
    @Transactional
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        var user = event.getInteraction().getUser().getId();
        return event.editReply(getReplyEditSpec(user, 0)).then();
    }

    public InteractionReplyEditSpec getReplyEditSpec(Snowflake userId, int page) {
        var pageSize = CommandConstants.PageSizeSmallItems;
        var usersPoints = new HashMap<User, Long>();
        AtomicReference<Long> mostPoints = new AtomicReference<>();
        AtomicReference<Integer> bestRank = new AtomicReference<>();
        AtomicReference<ExerciseType> bestExercise = new AtomicReference<>();

        var discordUser = gatewayDiscordClient.getUserById(userId).block();
        var user = userService.getOrCreateDiscordUser(userId);
        var allUsers = userService.getAllUsers();
        allUsers.forEach(u -> usersPoints.put(u, 0L));
        var allRecords = exerciseRecordService.getAll();
        allRecords
                .stream()
                .collect(Collectors.groupingBy(ExerciseRecord::getExerciseType))
                .entrySet()
                .forEach(gr -> {
            var exercise = gr.getKey();
            var comparator = Comparator.comparingDouble(ExerciseRecord::getAmount);
            if (exercise.getGoalDirection().equals(GoalDirection.INCREASING)) comparator = comparator.reversed();
            var records = gr.getValue().stream().sorted(comparator).limit(10).toList();

            for (int i = 0; i < records.size(); i++) {
                var points = (long) records.size() - i;
                var record = records.get(i);
                var recordUser = record.getUser();
                usersPoints.put(recordUser, usersPoints.getOrDefault(recordUser, 0L) + points);
                if (recordUser.equals(user)) {
                    if (mostPoints.get() == null || points > mostPoints.get()) {
                        mostPoints.set(points);
                        bestRank.set(i + 1);
                        bestExercise.set(exercise);
                    }
                }
            }
        });

        var rank = new AtomicInteger(0);
        var ranking = usersPoints.entrySet().stream()
                .map(v -> new UserRank(v.getKey(), v.getValue(), 0))
                .sorted(Comparator.comparingLong(UserRank::points).reversed())
                .map(v -> new UserRank(v.user, v.points, rank.incrementAndGet()))
                .toList();

        var description = new StringBuilder();
        description.append("### User position\n");
        var userRank = ranking.stream().filter(v -> v.user().equals(user)).findFirst();
        userRank.ifPresent(ur -> description.append(String.format("Your position: %s\n", CommandHandlerHelper.getLeaderboardValue(ur.rank))));
        if (bestExercise.get() != null && bestRank.get() != null && mostPoints.get() != null) {
            description.append(String.format("Best exercise: #%d %s\n", bestExercise.get().getId(), bestExercise.get().getName()));
            description.append(String.format("Reached position on this exercise: %s\n", CommandHandlerHelper.getLeaderboardValue(bestRank.get())));
            description.append(String.format("Gained points for this exercise: %d pts\n", mostPoints.get()));
        }

        // Create page
        var pageRequest = PageRequest.of(page, pageSize);
        var start = (int) pageRequest.getOffset();
        var end = Math.min((start + pageRequest.getPageSize()), ranking.size());
        var pageContent = ranking.subList(start, end);
        var rankingPage = new PageImpl<>(pageContent, pageRequest, ranking.size());

        description.append("### Global leaderboard");
        rankingPage.forEach(ur -> {
            var userName = CommandHandlerHelper.getUserStringValue(ur.user());
            description.append(String.format("\n**%s - %d pts - %s**", CommandHandlerHelper.getLeaderboardValue(ur.rank()), ur.points(), userName));
        });

        var embed = EmbedCreateSpec.builder()
                .author(discordUser.getUsername(), null, discordUser.getAvatarUrl())
                .description(description.toString())
                .color(Color.CYAN);

        var controls = CommandHandlerHelper.getPaginationComponent(page, rankingPage.getTotalPages(), getCommandNameFilter());
        return InteractionReplyEditSpec.builder().addEmbed(embed.build()).addComponent(controls).build();
    }

    private record UserRank(User user, long points, int rank) {}
}
