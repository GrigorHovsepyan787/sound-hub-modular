package com.example.service.impl;

import com.example.model.Song;
import com.example.model.SongPlay;
import com.example.projection.SongPopularity;
import com.example.repository.SongPlayRepository;
import com.example.repository.SongRepository;
import com.example.service.SongService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;
    private final SongPlayRepository songPlayRepository;

    @Override
    @Transactional
    public void registerPlay(Long songId) {
        Song song = songRepository.findById(songId).orElseThrow(EntityNotFoundException::new);
        SongPlay songPlay = new SongPlay();
        songPlay.setSong(song);
        songPlay.setPlayedAt(LocalDateTime.now());
        songPlayRepository.save(songPlay);
        song.incrementPlayCount();
    }

    @Override
    public Page<SongPopularity> getTopSongPopularityCurrentMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.last30Days();

        return songPlayRepository.findTopSongsForPeriod(
                month.start(),
                month.end(),
                pageable);
    }
}
