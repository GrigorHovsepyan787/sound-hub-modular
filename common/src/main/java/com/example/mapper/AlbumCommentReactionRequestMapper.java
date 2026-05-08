package com.example.mapper;

import com.example.dto.AlbumCommentReactionRequest;
import com.example.model.AlbumCommentReaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumCommentReactionRequestMapper {
    AlbumCommentReaction toEntity(AlbumCommentReactionRequest albumCommentReactionRequest);
}
