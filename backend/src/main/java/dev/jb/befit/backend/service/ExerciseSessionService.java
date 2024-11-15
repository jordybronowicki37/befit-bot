package dev.jb.befit.backend.service;

import dev.jb.befit.backend.data.ExerciseSessionRepository;
import dev.jb.befit.backend.data.models.ExerciseSession;
import dev.jb.befit.backend.data.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExerciseSessionService {
    private final ExerciseSessionRepository exerciseSessionRepository;

    public Page<ExerciseSession> getAllByUser(User user, Pageable pageable) {
        return exerciseSessionRepository.findAllByUserOrderByCreatedDesc(user, pageable);
    }

    public Optional<ExerciseSession> getLastByUser(User user) {
        return exerciseSessionRepository.findTopByUserOrderByIdDesc(user);
    }

    public Optional<ExerciseSession> getByUserAndId(User user, Long id) {
        return exerciseSessionRepository.findByUserAndId(user, id);
    }

    public Page<ExerciseSession> searchByNameAndUser(User user, String name, Pageable pageable) {
        return exerciseSessionRepository.findAllByUserAndNameIgnoreCaseContainingOrderByCreatedDesc(user, name, pageable);
    }

    public ExerciseSession create(User user, String name) {
        var session = new ExerciseSession(name, user);
        return exerciseSessionRepository.save(session);
    }
}
