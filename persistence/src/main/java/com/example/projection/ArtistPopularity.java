package com.example.projection;

import com.example.model.Artist;

public interface ArtistPopularity {
    Artist getArtist();

    Long getTotalPlays();
}
