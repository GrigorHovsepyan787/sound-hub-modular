package com.example.dto;

import com.example.validation.ValidAlbum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ValidAlbum
public class SaveAlbumDto {
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 50)
    private String title;

    @NotNull(message = "Release date is required")
    private LocalDateTime releaseDate;

    private Long artistId;
    private Long bandId;

    private List<Long> songIds;
}