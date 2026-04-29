package com.example.mapper;

import com.example.dto.SongCommentRequest;
import com.example.model.SongComment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongCommentRequestMapper {
    SongComment toEntity(SongCommentRequest songCommentRequest);
}
