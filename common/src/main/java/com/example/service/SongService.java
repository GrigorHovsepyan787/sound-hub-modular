package com.example.service;

import com.example.dto.SongDto;
import com.example.dto.SongPopularityDto;
import com.example.model.Genre;
import com.example.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SongService {
    Page<SongDto> findAll(Pageable pageable);

    Song save(Song song, MultipartFile multipartFile);

    void delete(Long id);

    Song getSongById(Long id);

    List<Integer> getPageNumbers(Page<SongDto> songs);

    Page<SongDto> findByGenre(Genre genre, Pageable pageable);

    Page<SongDto> findSongsByGenre(Genre genre, Pageable pageable);

    void registerPlay(Long songId);

    Page<SongPopularityDto> getTopSongPopularityLastMonth(Pageable pageable);

    List<SongDto> getTop5SongsOfArtistByPlayCount(Long artistId);

    List<SongDto> getTop5SongsOfBandByPlayCount(Long artistId);

    List<SongDto> getSongsByAlbumId(Long albumId);
}