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
public class ArtistDto {

    private Long id;
    private String name;
    private String surname;
    private String bio;
    private String pictureUrl;
    private String nickname;
    private LocalDate birthDate;
    private List<BandDto> bands;
}