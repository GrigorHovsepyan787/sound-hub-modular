package com.example.mapper;

import com.example.dto.AlbumDto;
import com.example.model.Album;
import com.example.model.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlbumMapper {
    @Mapping(target = "performerName", expression = "java(getPerformerName(album))")
    @Mapping(target = "songIds", source = "songs")
    @Mapping(target = "commentIds", ignore = true)
    AlbumDto toDto(Album album);

    default List<Long> mapSongsToIds(List<Song> songs) {
        if (songs == null) return List.of();
        return songs.stream().map(Song::getId).toList();
    }

    default String getPerformerName(Album album) {
        if (album.getArtist() != null) {
            return album.getArtist().getNickname();
        }
        if (album.getBand() != null) {
            return album.getBand().getName();
        }
        return null;
    }
}
