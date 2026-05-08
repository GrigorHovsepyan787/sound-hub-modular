package com.example.repository;

import com.example.model.SongComment;
import com.example.model.SongCommentReaction;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SongCommentReactionRepository extends JpaRepository<SongCommentReaction, Long> {
    Optional<SongCommentReaction> findByCommentAndUser(SongComment comment, User user);
}
