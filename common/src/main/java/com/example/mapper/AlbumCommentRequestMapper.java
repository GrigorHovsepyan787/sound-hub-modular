package com.example.mapper;

import com.example.dto.AlbumCommentRequest;
import com.example.model.AlbumComment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumCommentRequestMapper {
    AlbumComment toEntity(AlbumCommentRequest albumCommentRequest);
}
