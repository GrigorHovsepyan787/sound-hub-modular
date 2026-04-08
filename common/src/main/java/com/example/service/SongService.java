package com.example.service;

import com.example.projection.SongPopularity;
import com.example.dto.SongDto;
import com.example.model.Genre;
import com.example.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SongService {
    void registerPlay(Long songId);

    Page<SongPopularity> getTopSongPopularityLastMonth(Pageable pageable);
}
    Page<SongDto> findAll(Pageable pageable);

    Song save(Song song, MultipartFile multipartFile);

    void delete(Long id);

    Song getSongById(Long id);

    List<Integer> getPageNumbers(Page<SongDto> songs);

    void incrementPlayCount(Long id);

    Page<SongDto> findByGenre(Genre genre, Pageable pageable);

    Page<SongDto> findSongsByGenre(Genre genre, Pageable pageable);
}