package com.example.service.impl;

import com.example.dto.PlaylistDto;
import com.example.mapper.PlaylistMapper;
import com.example.model.Playlist;
import com.example.model.Song;
import com.example.model.User;
import com.example.repository.PlaylistRepository;
import com.example.repository.SongRepository;
import com.example.service.PlaylistService;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final StorageService storageService;
    private final PlaylistMapper playlistMapper;
    private final SongRepository songRepository;

    @Value("${playlist.default-image}")
    private String defaultImageUrl;

    @Override
    public List<PlaylistDto> findAll(Sort sort) {
        return playlistRepository.findAll(sort)
                .stream().map(playlistMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PlaylistDto create(PlaylistDto playlistDto, MultipartFile multipartFile, List<Long> songIds, User user) {
        Playlist playlist = playlistMapper.toEntity(playlistDto);
        playlist.setUser(user);

        String imageUrl;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = storageService.upload(multipartFile, "playlist-images");
        } else {
            imageUrl = defaultImageUrl;
        }
        playlist.setPictureUrl(imageUrl);

        List<Long> ids = songIds != null ? songIds : Collections.emptyList();
        playlist.setSongs(songRepository.findAllById(ids));

        Playlist savedPlaylist = playlistRepository.save(playlist);
        log.info("Created playlist '{}' for user '{}'", savedPlaylist.getName(), user.getUsername());
        return playlistMapper.toDto(savedPlaylist);
    }

    @Override
    @Transactional
    public PlaylistDto update(Long id, PlaylistDto playlistDto, MultipartFile multipartFile) {
        Playlist existingPlaylist = playlistRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        playlistMapper.updateEntityFromDto(playlistDto, existingPlaylist);

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "playlist-images");
            existingPlaylist.setPictureUrl(imageUrl);
            log.info("Image updated for playlist ID: {}", id);
        }

        return playlistMapper.toDto(playlistRepository.save(existingPlaylist));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        log.info("Deleting playlist ID: {}", id);
        playlistRepository.deleteById(id);
    }

    @Override
    public PlaylistDto getPlaylistById(Long id) {
        log.info("Fetching playlist ID: {}", id);
        Playlist playlist =  playlistRepository.findByIdWithSongs(id)
                .orElseThrow(EntityNotFoundException::new);
        return playlistMapper.toDto(playlist);
    }

    @Override
    @Transactional
    public void setVisibility(Long id, boolean isPublic) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        playlist.setPublicFlag(isPublic);

        playlistRepository.saveAndFlush(playlist);
    }

    @Override
    @Transactional
    public void addSong(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found: " + playlistId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found: " + songId));

        boolean alreadyAdded = playlist.getSongs().stream()
                .anyMatch(s -> s.getId().equals(songId));
        if (!alreadyAdded) {
            playlist.getSongs().add(song);
            playlistRepository.save(playlist);
            log.info("Added song {} to playlist {}", songId, playlistId);
        }
    }

    @Override
    @Transactional
    public void removeSong(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found: " + playlistId));

        boolean removed = playlist.getSongs().removeIf(s -> s.getId().equals(songId));
        if (removed) {
            playlistRepository.save(playlist);
            log.info("Removed song {} from playlist {}", songId, playlistId);
        }
    }

    @Override
    public String resolveCurrentSort(Sort sort) {
        return sort.stream()
                .findFirst()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .orElse("createdDate,desc");
    }
}
