package com.example.service.impl;

import com.example.dto.SongCommentReactionRequest;
import com.example.mapper.SongCommentReactionRequestMapper;
import com.example.model.SongComment;
import com.example.model.SongCommentReaction;
import com.example.model.User;
import com.example.repository.SongCommentReactionRepository;
import com.example.repository.SongCommentRepository;
import com.example.service.SongCommentReactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class SongCommentReactionServiceImpl implements SongCommentReactionService {
    private final SongCommentReactionRequestMapper songCommentReactionRequestMapper;
    private final SongCommentRepository songCommentRepository;
    private final SongCommentReactionRepository songCommentReactionRepository;

    @Override
    public void saveCommentReaction(SongCommentReactionRequest request, User user) {
        log.info("Attempting to create song comment reaction for commentId={} by user={}",
                request.getCommentId(),
                user.getUsername());
        SongComment comment = songCommentRepository.findById(request.getCommentId()).orElseThrow(() -> {
            log.warn("Song comment not found. commentId={}", request.getCommentId());
            return new EntityNotFoundException("Song comment not found");
        });
        Optional<SongCommentReaction> existing = songCommentReactionRepository.findByCommentAndUser(comment, user);

        if (existing.isPresent()) {
            SongCommentReaction reaction = existing.get();
            short oldValue = reaction.getValue();
            if (oldValue == request.getValue()) {
                log.warn("Attempt to enter the same reaction for song comment reactionId={} by userId={}",
                        reaction.getId(), user.getId());
                return;
            }
            reaction.setValue(request.getValue());
            comment.updateReaction(oldValue, reaction.getValue());
            log.info("Existing song comment reaction was edited successfully. reactionId={} by userId={}",
                    reaction.getId(), user.getId());
        } else {
            SongCommentReaction reaction = songCommentReactionRequestMapper.toEntity(request);
            reaction.setComment(comment);
            reaction.setUser(user);
            comment.setRating(comment.getRating() + reaction.getValue());
            songCommentReactionRepository.save(reaction);
            log.info("Song comment reaction was created successfully by userId={}",
                    user.getId());
        }
    }
}
