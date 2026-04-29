package com.example.service;

import com.example.dto.AlbumCommentReactionRequest;
import com.example.model.User;

public interface AlbumCommentReactionService {
    void saveCommentReaction(AlbumCommentReactionRequest request, User user);
}
