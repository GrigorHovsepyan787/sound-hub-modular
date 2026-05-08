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

    SongDto save(SongDto songDto, MultipartFile multipartFile);

    void delete(Long id);

    SongDto getSongById(Long id);

    List<Integer> getPageNumbers(Page<SongDto> songs);

    Page<SongDto> findSongsByGenre(Genre genre, Pageable pageable);

    void registerPlay(Long songId);

    Page<SongPopularityDto> getTopSongPopularityLastMonth(Pageable pageable);

    List<SongDto> getTop5SongsOfArtistByPlayCount(Long artistId);

    List<SongDto> getTop5SongsOfBandByPlayCount(Long artistId);

    List<SongDto> findTopByPlayCount(int limit);

    List<SongDto> getSongsByAlbumId(Long albumId);

    List<SongDto> searchSongs(String query, int limit);

}