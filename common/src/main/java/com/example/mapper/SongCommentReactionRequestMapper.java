package com.example.mapper;

import com.example.dto.SongCommentReactionRequest;
import com.example.model.SongCommentReaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongCommentReactionRequestMapper {
    SongCommentReaction toEntity(SongCommentReactionRequest songCommentReactionRequest);
}
