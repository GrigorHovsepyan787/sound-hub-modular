package com.example.projection;

import com.example.model.Song;

public interface SongPopularity {
    Song getSong();

    Long getTotalPlays();
}
