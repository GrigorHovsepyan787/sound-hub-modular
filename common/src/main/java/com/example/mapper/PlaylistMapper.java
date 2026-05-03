package com.example.mapper;

import com.example.dto.PlaylistDto;
import com.example.model.Playlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SongMapper.class, UserMapper.class})
public interface PlaylistMapper {

    PlaylistDto toDto(Playlist playlist);
}
