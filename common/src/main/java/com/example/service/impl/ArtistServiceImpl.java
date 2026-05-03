package com.example.service.impl;

import com.example.dto.ArtistDto;
import com.example.mapper.ArtistMapper;
import com.example.model.Artist;
import com.example.model.Band;
import com.example.projection.ArtistPopularity;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.service.ArtistService;
import com.example.storage.StorageService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final BandRepository bandRepository;
    private final StorageService storageService;
    private final SongPlayRepository songPlayRepository;
    private final ArtistMapper artistMapper;
    private final Clock clock = Clock.systemDefaultZone();
    @Value("${artist.default-image}")
    private String defaultImageUrl;

    @Override
    public Page<ArtistDto> findAll(Pageable pageable) {
        log.info("Fetching artists, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return artistRepository.findAll(pageable).map(artistMapper::toDto);
    }

    @Override
    public List<ArtistDto> findAll() {
        return artistRepository.findAll()
                .stream().map(artistMapper::toDto).toList();
    }

    @Override
    public ArtistDto save(ArtistDto artistDto, MultipartFile multipartFile, List<Long> bandIds) {
        Artist artist = artistMapper.toEntity(artistDto);

        String imageUrl;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = storageService.upload(multipartFile, "artist-images");
        } else {
            imageUrl = defaultImageUrl;
        }
        artist.setPictureUrl(imageUrl);

        if (bandIds != null && !bandIds.isEmpty()) {
            Set<Band> bands = bandIds.stream()
                    .map(id -> bandRepository.findById(id)
                            .orElseThrow(EntityNotFoundException::new))
                    .collect(Collectors.toSet());
            artist.setBands(bands);
        }

        return artistMapper.toDto(artistRepository.save(artist));
    }

    @Override
    public ArtistDto update(Long id, ArtistDto artistDto, MultipartFile multipartFile, List<Long> bandIds) {
        Artist existingArtist = artistRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        artistMapper.updateEntityFromDto(artistDto, existingArtist);

        existingArtist.getBands().clear();
        if (bandIds != null && !bandIds.isEmpty()) {
            Set<Band> managedBands = bandIds.stream()
                    .map(bandId -> bandRepository.findById(bandId)
                            .orElseThrow(EntityNotFoundException::new))
                    .collect(Collectors.toSet());
            existingArtist.getBands().addAll(managedBands);
        }

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "artist-images");
            existingArtist.setPictureUrl(imageUrl);
            log.info("Image updated for artist ID: {}", id);
        }
        return artistMapper.toDto(artistRepository.save(existingArtist));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting artist ID: {}", id);
        artistRepository.deleteById(id);
    }

    @Override
    public ArtistDto getArtistById(Long id) {
        log.info("Fetching artist ID: {}", id);
        return artistRepository.findById(id)
                .map(artistMapper::toDto)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Page<ArtistDto> getArtistsByName(String name, Pageable pageable) {
        if (StringUtils.isBlank(name)) {
            return artistRepository.findAll(pageable).map(artistMapper::toDto);
        }
        return artistRepository.findByNameContainingIgnoreCase(name, pageable).map(artistMapper::toDto);
    }

    @Override
    public Page<ArtistPopularity> getTopArtistPopularityLastMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.monthlyRange(clock);

        return songPlayRepository.findTopArtistsForPeriod(
                month.currentStart(),
                month.currentEnd(),
                pageable);
    }
}