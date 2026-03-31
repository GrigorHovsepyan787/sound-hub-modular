package com.example.service.impl;

import com.example.model.Artist;
import com.example.model.Band;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.service.ArtistService;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final BandRepository bandRepository;
    private final StorageService storageService;
    private static final String DEFAULT_ARTIST_IMAGE =
            "https://soundhub7.s3.eu-north-1.amazonaws.com/assets/ArtistDefault.png";

    @Override
    public Page<Artist> findAll(Pageable pageable) {
        log.info("Fetching artists, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return artistRepository.findAll(pageable);
    }

    @Override
    public Artist save(Artist artist, MultipartFile multipartFile, List<Long> bandIds) {
        String imageUrl;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = storageService.upload(multipartFile, "artist-images");
        } else {
            imageUrl = DEFAULT_ARTIST_IMAGE;
        }
        artist.setPictureUrl(imageUrl);

        if (bandIds != null && !bandIds.isEmpty()) {
            Set<Band> bands = bandIds.stream()
                    .map(id -> bandRepository.findById(id)
                            .orElseThrow(EntityNotFoundException::new))
                    .collect(Collectors.toSet());
            artist.setBands(bands);
        }

        return artistRepository.save(artist);
    }

    @Override
    public Artist update(Artist editedArtist, MultipartFile multipartFile, List<Long> bandIds) {
        Artist existingArtist = artistRepository.findById(editedArtist.getId())
                .orElseThrow(EntityNotFoundException::new);

        existingArtist.setName(editedArtist.getName());
        existingArtist.setSurname(editedArtist.getSurname());
        existingArtist.setBio(editedArtist.getBio());
        existingArtist.setNickname(editedArtist.getNickname());
        existingArtist.setBirthDate(editedArtist.getBirthDate());

        existingArtist.getBands().clear();
        if (bandIds != null && !bandIds.isEmpty()) {
            Set<Band> managedBands = bandIds.stream()
                    .map(id -> bandRepository.findById(id)
                            .orElseThrow(EntityNotFoundException::new))
                    .collect(Collectors.toSet());
            existingArtist.getBands().addAll(managedBands);
        }

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "artist-images");
            existingArtist.setPictureUrl(imageUrl);
            log.info("Image updated for artist ID: {}", editedArtist.getId());
        }
        return artistRepository.save(existingArtist);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting artist ID: {}", id);
        artistRepository.deleteById(id);
    }

    @Override
    public Artist getArtistById(Long id) {
        log.info("Fetching artist ID: {}", id);
        return artistRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Integer> getPageNumbers(Page<Artist> artists) {

        int totalPages = artists.getTotalPages();
        log.info("Total pages: {}", totalPages);

        if (totalPages == 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }

    @Override
    public Page<Artist> getArtistsByName(String name, Pageable pageable) {
        if(StringUtils.isBlank(name)) {
            return artistRepository.findAll(pageable);
        }
        return artistRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}