package com.example.mapper;

import com.example.dto.SaveAlbumDto;
import com.example.model.Album;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SaveAlbumMapper {
    Album toEntity(SaveAlbumDto saveAlbumDto);

    void updateEntity(@MappingTarget Album album, SaveAlbumDto dto);
}
