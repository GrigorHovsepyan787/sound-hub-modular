package com.example.mapper;

import com.example.dto.SongDto;
import com.example.model.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SongMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "artistFullName", expression = "java(song.getArtist() != null ? song.getArtist().getName() + \" \" + song.getArtist().getSurname() : null)")
    @Mapping(target = "artistNickname", expression = "java(song.getArtist() != null ? song.getArtist().getNickname() : null)")
    @Mapping(target = "bandName", source = "band.name")
    @Mapping(target = "bandId", source = "band.id")
    @Mapping(target = "artistId", source = "artist.id")
    @Mapping(target = "songUrl", source = "songUrl")
    @Mapping(target = "playCount", source = "playCount")
    SongDto toDto(Song song);
}