package com.example.service;

import com.example.dto.SongCommentReactionRequest;
import com.example.model.User;

public interface SongCommentReactionService {
    void saveCommentReaction(SongCommentReactionRequest request, User user);
}
