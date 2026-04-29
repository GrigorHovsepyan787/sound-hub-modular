package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PlaylistDto {

    private Long id;

    private String name;

    private UserDto user;

    private List<SongDto> songs;

    private boolean publicFlag;

    private String pictureUrl;

    private LocalDate createdDate;

    private int songCount;

}
