package com.example.service.impl;

import com.example.mapper.SongMapper;
import com.example.model.Album;
import com.example.projection.AlbumPopularity;
import com.example.repository.AlbumRepository;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.repository.SongRepository;
import com.example.service.AlbumService;
import com.example.storage.StorageService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final BandRepository bandRepository;
    private final ArtistRepository artistRepository;
    private final SongPlayRepository songPlayRepository;
    private final Clock clock = Clock.systemDefaultZone();
    private final SongRepository songRepository;
    private final SongMapper songMapper;
    @Value("${album.default-image}")
    private String defaultImageUrl;

    @Override
    public Page<Album> findAlbumPage(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    @Override
    public List<Album> findAll() {
        return albumRepository.findAll();
    }

    @Override
    @Transactional
    public void save(Album album, MultipartFile multipartFile, Long bandId, Long artistId) {
        if (bandId == null && artistId == null) {
            throw new IllegalArgumentException("Parameter 'bandId' or 'artistId' must not be null");
        }
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "album-images");

            if (imageUrl != null) {
                album.setPictureUrl(imageUrl);
                log.info("Image uploaded for album: {}", album.getTitle());
            }
        } else {
            album.setPictureUrl(defaultImageUrl);
        }
        if (bandId != null) {
            album.setBand(bandRepository.findById(bandId).orElseThrow(EntityNotFoundException::new));
        }
        if (artistId != null) {
            album.setArtist(artistRepository.findById(artistId).orElseThrow(EntityNotFoundException::new));
        }
        albumRepository.save(album);
    }

    @Override
    public Album findAlbumById(Long id) {
        return albumRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    @Transactional
    public void update(Album album, MultipartFile multipartFile, Long bandId, Long artistId) {
        Album existingAlbum = findAlbumById(album.getId());
        existingAlbum.setTitle(album.getTitle());
        if (bandId != null) {
            existingAlbum.setBand(bandRepository.findById(bandId).orElseThrow(EntityNotFoundException::new));
            existingAlbum.setArtist(null);
        }
        if (artistId != null) {
            existingAlbum.setArtist(artistRepository.findById(artistId).orElseThrow(EntityNotFoundException::new));
            existingAlbum.setBand(null);
        }

        existingAlbum.setReleaseDate(album.getReleaseDate());
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "album-images");
            existingAlbum.setPictureUrl(imageUrl);
        }
        albumRepository.save(existingAlbum);
    }

    @Override
    public Page<AlbumPopularity> getTopAlbumPopularityLastMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.monthlyRange(clock);

        return songPlayRepository.findTopAlbumsForPeriod(
                month.currentStart(),
                month.currentEnd(),
                pageable);
    }

    @Override
    public Page<Album> findByArtistIsNotNull(Pageable pageable) {
        return albumRepository.findByArtistIsNotNull(pageable);
    }

    @Override
    public Page<Album> findByBandIsNotNull(Pageable pageable) {
        return albumRepository.findByBandIsNotNull(pageable);
    }
}

