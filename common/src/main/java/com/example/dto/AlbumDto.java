package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDto {
    private Long id;
    private String title;
    private LocalDateTime releaseDate;
    private String performerName;
    private List<Long> songIds;
    private List<Long> commentIds;
}
