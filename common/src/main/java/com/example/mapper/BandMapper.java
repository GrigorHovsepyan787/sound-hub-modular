package com.example.mapper;

import com.example.dto.ArtistDto;
import com.example.dto.BandDto;
import com.example.model.Artist;
import com.example.model.Band;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BandMapper {

    BandDto toDto(Band band);

    @Mapping(target = "bands", ignore = true)
    ArtistDto toShallowArtistDto(Artist artist);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "artists", ignore = true)
    Band toEntity(BandDto bandDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "artists", ignore = true)
    void updateEntityFromDto(BandDto bandDto, @MappingTarget Band band);
}