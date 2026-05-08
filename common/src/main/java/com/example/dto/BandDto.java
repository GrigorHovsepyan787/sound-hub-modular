package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BandDto {

    private Long id;
    private String name;
    private String bio;
    private String pictureUrl;
    private LocalDate createdDate;
    private List<ArtistDto> artists;
}