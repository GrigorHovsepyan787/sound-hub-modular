package com.example.service.impl;

import com.example.dto.AlbumDto;
import com.example.dto.SaveAlbumDto;
import com.example.mapper.AlbumMapper;
import com.example.mapper.SaveAlbumMapper;
import com.example.model.Album;
import com.example.model.AlbumComment;
import com.example.model.Artist;
import com.example.model.Band;
import com.example.projection.AlbumPopularity;
import com.example.repository.AlbumCommentRepository;
import com.example.repository.AlbumRepository;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.service.AlbumService;
import com.example.storage.StorageService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
    private final AlbumMapper albumMapper;
    private final SaveAlbumMapper saveAlbumMapper;
    private final AlbumCommentRepository albumCommentRepository;
    private final Clock clock = Clock.systemDefaultZone();
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
    public Page<AlbumDto> findAllDto(Pageable pageable) {
        return albumRepository.findAll(pageable)
                .map(album -> {
                    AlbumDto dto = albumMapper.toDto(album);

                    List<Long> commentIds = albumCommentRepository
                            .findByAlbumId(album.getId())
                            .stream()
                            .map(AlbumComment::getId)
                            .toList();

                    dto.setCommentIds(commentIds);

                    return dto;
                });
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
    @Transactional
    public AlbumDto saveDto(SaveAlbumDto dto) {
        Artist artist = null;
        Band band = null;

        if (dto.getArtistId() != null) {
            artist = artistRepository.findById(dto.getArtistId())
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found"));
        }

        if (dto.getBandId() != null) {
            band = bandRepository.findById(dto.getBandId())
                    .orElseThrow(() -> new EntityNotFoundException("Band not found"));
        }

        Album album = saveAlbumMapper.toEntity(dto);

        album.setArtist(artist);
        album.setBand(band);
        Album saved = albumRepository.save(album);
        log.info("Saved album: {}", dto.getTitle());
        return albumMapper.toDto(saved);
    }

    @Override
    public Album findAlbumById(Long id) {
        return albumRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public AlbumDto findAlbumDtoById(Long id) {
        AlbumDto dto = albumMapper.toDto(findAlbumById(id));
        List<Long> commentIds = albumCommentRepository
                .findByAlbumId(id)
                .stream()
                .map(AlbumComment::getId)
                .toList();
        dto.setCommentIds(commentIds);
        return dto;
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

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long id) {
        Album album = albumRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        albumRepository.delete(album);
        log.info("Deleted album: {}", album.getTitle());
    }

    @Override
    public AlbumDto updateAlbumDto(SaveAlbumDto dto, Long id) {
        Album existing = albumRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        saveAlbumMapper.updateEntity(existing, dto);

        Album saved = albumRepository.save(existing);

        return albumMapper.toDto(saved);
    }
}

