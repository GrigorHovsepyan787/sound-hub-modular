package com.example.service;

import com.example.dto.PlaylistDto;
import com.example.model.Playlist;
import com.example.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlaylistService {

    List<Playlist> findAll(Sort sort);

    Playlist create(Playlist playlist, MultipartFile multipartFile, List<Long> songIds);

    Playlist update(Playlist editedPlaylist, MultipartFile multipartFile);

    void delete(Long id);

    void createDefaultPlaylist(User user);

    PlaylistDto getPlaylistById(Long id);

    void setVisibility(Long id, boolean isPublic);

    void addSong(Long playlistId, Long songId);

    void removeSong(Long playlistId, Long songId);

    String resolveCurrentSort(Sort sort);
}
