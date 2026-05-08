package com.example.service.impl;

import com.example.dto.SongDto;
import com.example.dto.SongPopularityDto;
import com.example.mapper.SongMapper;
import com.example.mapper.SongPopularityMapper;
import com.example.model.Album;
import com.example.model.Artist;
import com.example.model.Band;
import com.example.model.Genre;
import com.example.model.Song;
import com.example.model.SongPlay;
import com.example.repository.SongPlayRepository;
import com.example.repository.SongRepository;
import com.example.service.SongService;
import com.example.storage.StorageService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;
    private final SongPopularityMapper songPopularityMapper;
    private final StorageService storageService;
    private final SongPlayRepository songPlayRepository;
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public Page<SongDto> findAll(Pageable pageable) {
        log.info("Fetching songs, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Song> songsPage = songRepository.findAll(pageable);
        return songsPage.map(songMapper::toDto);
    }

    @Override
    public SongDto save(SongDto songDto, MultipartFile multipartFile) {
        Song song = songMapper.toEntity(songDto);
        song.setSongUrl(storageService.upload(multipartFile, "songs"));
        song.setDuration(getDuration(multipartFile));

        if (songDto.getArtistId() != null) {
            Artist artist = new Artist();
            artist.setId(songDto.getArtistId());
            song.setArtist(artist);
        } else {
            song.setArtist(null);
        }

        if (songDto.getBandId() != null) {
            Band band = new Band();
            band.setId(songDto.getBandId());
            song.setBand(band);
        } else {
            song.setBand(null);
        }

        if (songDto.getAlbumId() != null) {
            Album album = new Album();
            album.setId(songDto.getAlbumId());
            song.setAlbum(album);
        } else {
            song.setAlbum(null);
        }

        Song savedSong = songRepository.save(song);
        return songMapper.toDto(savedSong);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting song ID: {}", id);
        songPlayRepository.deleteBySongId(id);
        songRepository.deleteById(id);
    }

    @Override
    public SongDto getSongById(Long id) {
        log.info("Fetching song ID: {}", id);
        return songRepository.findById(id)
                .map(songMapper::toDto)
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
    }

    @Override
    @Transactional
    public void registerPlay(Long songId) {
        Song song = songRepository.findById(songId).orElseThrow(EntityNotFoundException::new);
        SongPlay songPlay = new SongPlay();
        songPlay.setSong(song);
        songPlay.setPlayedAt(LocalDateTime.now());
        songPlayRepository.save(songPlay);
        songRepository.incrementPlayCount(songId);
    }

    public Page<SongPopularityDto> getTopSongPopularityLastMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.monthlyRange(clock);
        return songPlayRepository.findTopSongsForPeriod(
                        month.currentStart(),
                        month.currentEnd(),
                        pageable)
                .map(songPopularityMapper::toDto);
    }

    @Override
    public List<SongDto> getTop5SongsOfArtistByPlayCount(Long artistId) {
        return songRepository.findTop5ByArtistIdOrderByPlayCountDesc(artistId)
                .stream()
                .map(songMapper::toDto)
                .toList();
    }

    @Override
    public List<SongDto> getTop5SongsOfBandByPlayCount(Long artistId) {
        return songRepository.findTop5ByBandIdOrderByPlayCountDesc(artistId)
                .stream()
                .map(songMapper::toDto)
                .toList();
    }

    @Override
    public List<SongDto> findTopByPlayCount(int limit) {
        return songRepository.findTopByPlayCount(PageRequest.of(0, limit))
                .stream()
                .map(songMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDto> getSongsByAlbumId(Long albumId) {
        return songRepository.findByAlbumId(albumId)
                .stream()
                .map(songMapper::toDto)
                .toList();
    }

    @Override
    public List<SongDto> searchSongs(String query, int limit) {
        if (query == null || query.isBlank()) {
            return findTopByPlayCount(limit);
        }

        log.info("Searching songs. query='{}', limit={}", query, limit);

        return songRepository.findByTitleContainingIgnoreCase(query, PageRequest.of(0, limit))
                .stream()
                .map(songMapper::toDto)
                .toList();
    }
}
