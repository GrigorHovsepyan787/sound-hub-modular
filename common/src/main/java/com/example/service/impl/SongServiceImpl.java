package com.example.service.impl;

import com.example.model.Song;
import com.example.model.SongPlay;
import com.example.repository.SongPlayRepository;
import com.example.repository.SongRepository;
import com.example.service.SongService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
