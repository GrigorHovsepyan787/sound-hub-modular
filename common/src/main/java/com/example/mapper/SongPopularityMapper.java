package com.example.mapper;

import com.example.dto.SongPopularityDto;
import com.example.projection.SongPopularity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface SongPopularityMapper {

    SongPopularityDto toDto(SongPopularity projection);

}