package com.example.service.impl;

import com.example.dto.AlbumCommentReactionRequest;
import com.example.mapper.AlbumCommentReactionRequestMapper;
import com.example.model.AlbumComment;
import com.example.model.AlbumCommentReaction;
import com.example.model.User;
import com.example.repository.AlbumCommentReactionRepository;
import com.example.repository.AlbumCommentRepository;
import com.example.service.AlbumCommentReactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AlbumCommentReactionServiceImpl implements AlbumCommentReactionService {
    private final AlbumCommentReactionRequestMapper albumCommentReactionRequestMapper;
    private final AlbumCommentRepository albumCommentRepository;
    private final AlbumCommentReactionRepository albumCommentReactionRepository;

    @Override
    public void saveCommentReaction(AlbumCommentReactionRequest request, User user) {
        log.info("Attempting to create album comment reaction for commentId={} by user={}",
                request.getCommentId(),
                user.getUsername());
        AlbumComment comment = albumCommentRepository.findById(request.getCommentId()).orElseThrow(() -> {
            log.warn("Album comment not found. commentId={}", request.getCommentId());
            return new EntityNotFoundException("Album comment not found");
        });
        Optional<AlbumCommentReaction> existing = albumCommentReactionRepository.findByCommentAndUser(comment, user);

        if (existing.isPresent()) {
            AlbumCommentReaction reaction = existing.get();
            short oldValue = reaction.getValue();
            if (oldValue == request.getValue()) {
                log.warn("Attempt to enter the same reaction for album comment reactionId={} by userId={}",
                        reaction.getId(), user.getId());
                return;
            }
            reaction.setValue(request.getValue());
            comment.setRating(comment.getRating() - oldValue + reaction.getValue());
            log.info("Existing album comment reaction was edited successfully. reactionId={} by userId={}",
                    reaction.getId(), user.getId());
        } else {
            AlbumCommentReaction reaction = albumCommentReactionRequestMapper.toEntity(request);
            reaction.setComment(comment);
            reaction.setUser(user);
            comment.setRating(comment.getRating() + reaction.getValue());
            albumCommentReactionRepository.save(reaction);
            log.info("Album comment reaction was created successfully by userId={}",
                    user.getId());
        }
    }
}
