package com.example.mapper;

import com.example.dto.SongCommentDto;
import com.example.model.SongComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SongCommentMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "songId", source = "song.id")
    SongCommentDto toDto(SongComment songComment);
}
