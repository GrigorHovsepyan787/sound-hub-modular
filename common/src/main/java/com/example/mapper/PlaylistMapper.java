package com.example.mapper;

import com.example.dto.PlaylistDto;
import com.example.model.Playlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SongMapper.class, UserMapper.class})
public interface PlaylistMapper {

    PlaylistDto toDto(Playlist playlist);

    Playlist toEntity(PlaylistDto playlistDto);

    List<PlaylistDto> toDtoList(List<Playlist> playlists);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "songCount", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    void updateEntityFromDto(PlaylistDto dto, @MappingTarget Playlist playlist);
}
