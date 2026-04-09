package com.example.mapper;


import com.example.dto.SongPlayDto;
import com.example.model.SongPlay;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface SongPlayMapper {
    SongPlayDto toDto(SongPlay songPlay);
}