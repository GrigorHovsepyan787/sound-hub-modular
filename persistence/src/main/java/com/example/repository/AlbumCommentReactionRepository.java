package com.example.repository;

import com.example.model.AlbumComment;
import com.example.model.AlbumCommentReaction;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumCommentReactionRepository extends JpaRepository<AlbumCommentReaction, Long> {
    Optional<AlbumCommentReaction> findByCommentAndUser(AlbumComment comment, User user);
}
