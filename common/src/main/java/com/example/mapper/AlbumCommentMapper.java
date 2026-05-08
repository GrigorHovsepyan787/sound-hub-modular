package com.example.mapper;

import com.example.dto.AlbumCommentDto;
import com.example.model.AlbumComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlbumCommentMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "albumId", source = "album.id")
    AlbumCommentDto toDto(AlbumComment albumComment);
}
