package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseSessionRepository;
import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.ExerciseSessionStatus;
import dev.jb.befit.backend.data.models.User;
import dev.jb.befit.backend.service.exceptions.SessionNotFoundException;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExerciseSessionService {
    private final ExerciseSessionRepository exerciseSessionRepository;

    public Long amountByUser(User user) {
        return exerciseSessionRepository.countAllByUser(user);
    }

    public Page<ExerciseSession> getAllByUser(User user, Pageable pageable) {
        return exerciseSessionRepository.findAllByUserOrderByCreatedDesc(user, pageable);
    }

    public List<ExerciseSession> getAllActiveAndOutdated() {
        return exerciseSessionRepository.findAllByStatusAndEndedBeforeOrderByCreatedAsc(ExerciseSessionStatus.ACTIVE, LocalDateTime.now());
    }

    public Page<ExerciseSession> getAllActiveByUser(User user, Pageable pageable) {
        return exerciseSessionRepository.findAllByUserAndStatusOrderByCreatedDesc(user, ExerciseSessionStatus.ACTIVE, pageable);
    }

    public Optional<ExerciseSession> getLastByUser(User user) {
        return exerciseSessionRepository.findTopByUserOrderByIdDesc(user);
    }

    public Optional<ExerciseSession> getLastActiveByUser(User user) {
        return exerciseSessionRepository.findTopByUserAndStatusOrderByIdDesc(user, ExerciseSessionStatus.ACTIVE);
    }

    public Optional<ExerciseSession> getByUserAndId(User user, Long id) {
        return exerciseSessionRepository.findByUserAndId(user, id);
    }

    public Page<ExerciseSession> searchByNameAndUser(User user, String name, Pageable pageable) {
        return exerciseSessionRepository.findAllByUserAndNameIgnoreCaseContainingOrderByCreatedDesc(user, name, pageable);
    }

    public ExerciseSession create(User user, String name) {
        return create(user, name, null);
    }

    public ExerciseSession create(User user, String name, Snowflake channelId) {
        var lastSession = getLastActiveByUser(user);
        lastSession.ifPresent(s -> {
            s.setStatus(ExerciseSessionStatus.OVERWRITTEN);
            exerciseSessionRepository.save(s);
        });

        var session = new ExerciseSession(name, user);
        session.setDiscordChannelId(channelId);
        return exerciseSessionRepository.save(session);
    }

    public ExerciseSession extendAutomaticFinalization(User user, Long id) {
        var session = getByUserAndId(user, id).orElseThrow(() -> new SessionNotFoundException(id));
        if (!session.getStatus().equals(ExerciseSessionStatus.ACTIVE)) return session;

        session.setEnded(LocalDateTime.now().plusSeconds(ServiceConstants.SessionTimeout.getEpochSecond()));

        return exerciseSessionRepository.save(session);
    }

    public ExerciseSession updateStatus(User user, Long id, ExerciseSessionStatus status) {
        var session = getByUserAndId(user, id).orElseThrow(() -> new SessionNotFoundException(id));
        session.setStatus(status);
        if (!status.equals(ExerciseSessionStatus.ACTIVE)) session.setEnded(LocalDateTime.now());
        return exerciseSessionRepository.save(session);
    }

    public ExerciseSession updateRating(User user, Long id, Integer rating) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be from 1 to 5");
        var session = getByUserAndId(user, id).orElseThrow(() -> new SessionNotFoundException(id));
        session.setRating(rating);
        return exerciseSessionRepository.save(session);
    }
}
