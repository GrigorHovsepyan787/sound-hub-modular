package com.example.service.impl;

import com.example.dto.SongDto;
import com.example.mapper.SongMapper;
import com.example.model.Genre;
import com.example.model.Song;
import com.example.model.SongPlay;
import com.example.projection.SongPopularity;
import com.example.repository.SongPlayRepository;
import com.example.repository.SongRepository;
import com.example.service.SongService;
import com.example.storage.StorageService;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;
    private final StorageService storageService;
    private final SongPlayRepository songPlayRepository;

    @Override
    public Page<SongDto> findAll(Pageable pageable) {
        log.info("Fetching songs, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Song> songsPage = songRepository.findAll(pageable);
        return songsPage.map(songMapper::toDto);
    }

    @Override
    public Song save(Song song, MultipartFile multipartFile) {
        String songUrl = storageService.upload(multipartFile, "songs");
        song.setSongUrl(songUrl);
        song.setDuration(getDuration(multipartFile));
        return songRepository.save(song);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting song ID: {}", id);
        songRepository.deleteById(id);
    }

    @Override
    public Song getSongById(Long id) {
        log.info("Fetching song ID: {}", id);
        return songRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }


    @Override
    public List<Integer> getPageNumbers(Page<SongDto> songs) {

        int totalPages = songs.getTotalPages();
        log.info("Total pages: {}", totalPages);

        if (totalPages == 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
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
    public void incrementPlayCount(Long id) {
        songRepository.incrementPlayCount(id);
    }

    @Override
    public Page<SongDto> findByGenre(Genre genre, Pageable pageable) {
        return songRepository.findByGenre(genre, pageable)
                .map(songMapper::toDto);
    }

    @Override
    public Page<SongDto> findSongsByGenre(Genre genre, Pageable pageable) {
        if (genre != null) {
            log.info("Fetching songs by genre: {}", genre);
            return songRepository.findByGenre(genre, pageable).map(songMapper::toDto);
        }

        log.info("Fetching all songs");
        return songRepository.findAll(pageable).map(songMapper::toDto);
    }
    public Page<SongPopularity> getTopSongPopularityLastMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.last30Days();

    private int getDuration(MultipartFile multipartFile) {
        try {
            File temp = File.createTempFile("upload-", "mp3");
            multipartFile.transferTo(temp);

            Mp3File mp3File = new Mp3File(temp);
            long duration = mp3File.getLengthInSeconds();
            temp.delete();
            return (int) duration;
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            log.warn("Could not extract duration for file: {}", multipartFile.getOriginalFilename(), e);
            return 0;
        }
        return songPlayRepository.findTopSongsForPeriod(
                month.start(),
                month.end(),
                pageable);
    }
}
