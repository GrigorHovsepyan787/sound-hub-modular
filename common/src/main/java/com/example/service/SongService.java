package com.example.service;

import com.example.projection.SongPopularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SongService {
    void registerPlay(Long songId);

    Page<SongPopularity> getTopSongPopularityCurrentMonth(Pageable pageable);
}
