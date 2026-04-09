package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SongDto {
    private Long id;
    private String title;
    private String artistFullName;
    private String artistNickname;
    private String bandName;
    private String genre;
    private int duration;
    private int playCount;
    private Long bandId;
    private Long artistId;
    private String songUrl;
    private String pictureUrl;

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