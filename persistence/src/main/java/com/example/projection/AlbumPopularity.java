package com.example.projection;

import com.example.model.Album;

public interface AlbumPopularity {
    Album getAlbum();

    Long getTotalPlays();
}
