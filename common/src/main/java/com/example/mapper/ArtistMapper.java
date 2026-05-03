package com.example.mapper;

import com.example.dto.ArtistDto;
import com.example.dto.BandDto;
import com.example.model.Artist;
import com.example.model.Band;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    ArtistDto toDto(Artist artist);

    @Mapping(target = "artists", ignore = true)
    BandDto toShallowBandDto(Band band);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bands", ignore = true)
    Artist toEntity(ArtistDto artistDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bands", ignore = true)
    void updateEntityFromDto(ArtistDto artistDto, @MappingTarget Artist artist);
}