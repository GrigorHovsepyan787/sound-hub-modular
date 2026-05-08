package com.example.dto;

import com.example.model.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SongDto {
    private Long id;
    private String title;
    private Genre genre;
    private int duration;
    private int playCount;
    private LocalDate uploadDate;
    private String songUrl;
    private String pictureUrl;
    private List<Long> commentIds;

    private Long artistId;
    private String artistFullName;
    private String artistNickname;

    private Long bandId;
    private String bandName;

    private Long albumId;
    private String albumName;

    public String getDurationFormatted() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%2d:%02d", minutes, seconds);
    }

    public String getPerformer() {
        if (bandName != null && !bandName.isBlank()) {
            return bandName;
        }

        if (artistNickname != null && !artistNickname.isBlank()) {
            return artistNickname;
        }

        return artistFullName;
    }
}