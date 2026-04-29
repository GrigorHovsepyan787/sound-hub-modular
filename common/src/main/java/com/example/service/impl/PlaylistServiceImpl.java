package com.example.service.impl;

import com.example.dto.PlaylistDto;
import com.example.mapper.PlaylistMapper;
import com.example.mapper.SongMapper;
import com.example.model.Playlist;
import com.example.model.Song;
import com.example.model.User;
import com.example.repository.PlaylistRepository;
import com.example.repository.SongRepository;
import com.example.service.PlaylistService;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final StorageService storageService;
    @Value("${playlist.default-image}")
    private String defaultImageUrl;
    private final PlaylistMapper playlistMapper;
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Override
    public List<Playlist> findAll(Sort sort) {
        return playlistRepository.findAll(sort);
    }

    @Override
    public Playlist create(Playlist playlist, MultipartFile multipartFile, List<Long> songIds) {
        String imageUrl;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = storageService.upload(multipartFile, "playlist-images");
        } else {
            imageUrl = defaultImageUrl;
        }
        playlist.setPictureUrl(imageUrl);

        List<Long> ids = songIds != null ? songIds : Collections.emptyList();
        playlist.setSongs(songRepository.findAllById(ids));

        return playlistRepository.save(playlist);
    }

    @Override
    public Playlist update(Playlist editedPlaylist, MultipartFile multipartFile) {
        Playlist existingPlaylist = playlistRepository.findById(editedPlaylist.getId())
                .orElseThrow(EntityNotFoundException::new);

        existingPlaylist.setName(editedPlaylist.getName());
        existingPlaylist.setPublicFlag(editedPlaylist.isPublicFlag());

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "playlist-images");
            existingPlaylist.setPictureUrl(imageUrl);
            log.info("Image updated for playlist ID: {}", editedPlaylist.getId());
        }

        return playlistRepository.save(existingPlaylist);
    }

    @Override
    public void delete(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        if (playlist.getIsDefault()) {
            throw new IllegalArgumentException("Cannot delete default playlist");
        }
        log.info("Deleting playlist ID: {}", id);
        playlistRepository.deleteById(id);
    }

    @Override
    public void createDefaultPlaylist(User user) {
        Playlist favorites = new Playlist();
        favorites.setName("Favorite Songs");
        favorites.setUser(user);
        favorites.setIsDefault(true);

        playlistRepository.save(favorites);
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
}
