package com.example.service.impl;

import com.example.dto.PlaylistDto;
import com.example.mapper.PlaylistMapper;
import com.example.model.Playlist;
import com.example.model.Song;
import com.example.model.User;
import com.example.repository.PlaylistRepository;
import com.example.repository.SongRepository;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    @Mock private PlaylistRepository playlistRepository;
    @Mock private StorageService storageService;
    @Mock private PlaylistMapper playlistMapper;
    @Mock private SongRepository songRepository;

    @InjectMocks private PlaylistServiceImpl playlistService;

    @Test
    void findAll_happyPath_returnsSortedList() {
        Sort sort = Sort.by("name");
        List<Playlist> playlists = List.of(new Playlist());
        when(playlistRepository.findAll(sort)).thenReturn(playlists);

        List<Playlist> result = playlistService.findAll(sort);

        assertThat(result).isEqualTo(playlists);
    }

    @Test
    void create_withFileAndSongIds_uploadsImageAndSetsSongs() {
        Playlist playlist = new Playlist();
        MultipartFile file = mock(MultipartFile.class);
        Song song = new Song();
        when(file.isEmpty()).thenReturn(false);
        when(storageService.upload(file, "playlist-images")).thenReturn("http://img.url");
        when(songRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(song));
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        Playlist result = playlistService.create(playlist, file, List.of(1L, 2L));

        assertThat(playlist.getPictureUrl()).isEqualTo("http://img.url");
        assertThat(playlist.getSongs()).containsExactly(song);
        assertThat(result).isEqualTo(playlist);
    }

    @Test
    void create_withNullFile_usesDefaultImage() {
        Playlist playlist = new Playlist();
        when(songRepository.findAllById(List.of())).thenReturn(List.of());
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        playlistService.create(playlist, null, null);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void create_withNullSongIds_usesEmptyList() {
        Playlist playlist = new Playlist();
        when(songRepository.findAllById(List.of())).thenReturn(List.of());
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        playlistService.create(playlist, null, null);

        verify(songRepository).findAllById(List.of());
    }

    @Test
    void update_withFile_updatesFieldsAndImage() {
        Playlist existing = new Playlist();
        existing.setId(1L);
        Playlist edited = new Playlist();
        edited.setId(1L);
        edited.setName("New Name");
        edited.setPublicFlag(true);
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storageService.upload(file, "playlist-images")).thenReturn("http://new-img.url");
        when(playlistRepository.save(existing)).thenReturn(existing);

        Playlist result = playlistService.update(edited, file);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.isPublicFlag()).isTrue();
        assertThat(existing.getPictureUrl()).isEqualTo("http://new-img.url");
        assertThat(result).isEqualTo(existing);
    }

    @Test
    void update_withNullFile_doesNotUploadImage() {
        Playlist existing = new Playlist();
        existing.setId(1L);
        Playlist edited = new Playlist();
        edited.setId(1L);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(playlistRepository.save(existing)).thenReturn(existing);

        playlistService.update(edited, null);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void update_playlistNotFound_throwsEntityNotFoundException() {
        Playlist edited = new Playlist();
        edited.setId(99L);
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.update(edited, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_nonDefaultPlaylist_deletesById() {
        Playlist playlist = new Playlist();
        playlist.setIsDefault(false);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        playlistService.delete(1L);

        verify(playlistRepository).deleteById(1L);
    }

    @Test
    void delete_defaultPlaylist_throwsIllegalArgumentException() {
        Playlist playlist = new Playlist();
        playlist.setIsDefault(true);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        assertThatThrownBy(() -> playlistService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete default playlist");

        verify(playlistRepository, never()).deleteById(any());
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createDefaultPlaylist_createsAndSavesFavoritesPlaylist() {
        User user = new User();

        playlistService.createDefaultPlaylist(user);

        verify(playlistRepository).save(argThat(p ->
                "Favorite Songs".equals(p.getName()) &&
                        p.getUser() == user &&
                        p.getIsDefault()
        ));
    }

    @Test
    void getPlaylistById_exists_returnsDto() {
        Playlist playlist = new Playlist();
        PlaylistDto dto = new PlaylistDto();
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(playlist));
        when(playlistMapper.toDto(playlist)).thenReturn(dto);

        PlaylistDto result = playlistService.getPlaylistById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getPlaylistById_notFound_throwsEntityNotFoundException() {
        when(playlistRepository.findByIdWithSongs(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.getPlaylistById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void setVisibility_happyPath_updatesPublicFlag() {
        Playlist playlist = new Playlist();
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        playlistService.setVisibility(1L, true);

        assertThat(playlist.isPublicFlag()).isTrue();
        verify(playlistRepository).saveAndFlush(playlist);
    }

    @Test
    void setVisibility_notFound_throwsEntityNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.setVisibility(99L, true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addSong_songNotInPlaylist_addsSong() {
        Playlist playlist = new Playlist();
        playlist.setSongs(new ArrayList<>());
        Song song = new Song();
        song.setId(10L);
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(10L)).thenReturn(Optional.of(song));
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        playlistService.addSong(1L, 10L);

        assertThat(playlist.getSongs()).contains(song);
        verify(playlistRepository).save(playlist);
    }

    @Test
    void addSong_songAlreadyInPlaylist_doesNotAddDuplicate() {
        Song song = new Song();
        song.setId(10L);
        Playlist playlist = new Playlist();
        playlist.setSongs(new ArrayList<>(List.of(song)));
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(10L)).thenReturn(Optional.of(song));

        playlistService.addSong(1L, 10L);

        assertThat(playlist.getSongs()).hasSize(1);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void addSong_playlistNotFound_throwsEntityNotFoundException() {
        when(playlistRepository.findByIdWithSongs(99L))
                .thenThrow(new EntityNotFoundException("Playlist not found: 99"));

        assertThatThrownBy(() -> playlistService.addSong(99L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addSong_songNotFound_throwsEntityNotFoundException() {
        Playlist playlist = new Playlist();
        playlist.setSongs(new ArrayList<>());
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.addSong(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeSong_songInPlaylist_removesSong() {
        Song song = new Song();
        song.setId(10L);
        Playlist playlist = new Playlist();
        playlist.setSongs(new ArrayList<>(List.of(song)));
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(playlist));
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        playlistService.removeSong(1L, 10L);

        assertThat(playlist.getSongs()).isEmpty();
        verify(playlistRepository).save(playlist);
    }

    @Test
    void removeSong_songNotInPlaylist_doesNotSave() {
        Song song = new Song();
        song.setId(5L);
        Playlist playlist = new Playlist();
        playlist.setSongs(new ArrayList<>(List.of(song)));
        when(playlistRepository.findByIdWithSongs(1L)).thenReturn(Optional.of(playlist));

        playlistService.removeSong(1L, 99L);

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void removeSong_playlistNotFound_throwsEntityNotFoundException() {
        when(playlistRepository.findByIdWithSongs(99L))
                .thenThrow(new EntityNotFoundException("Playlist not found: 99"));

        assertThatThrownBy(() -> playlistService.removeSong(99L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void resolveCurrentSort_withSort_returnsFormattedString() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        String result = playlistService.resolveCurrentSort(sort);

        assertThat(result).isEqualTo("name,asc");
    }

    @Test
    void resolveCurrentSort_withUnsortedSort_returnsDefault() {
        Sort sort = Sort.unsorted();

        String result = playlistService.resolveCurrentSort(sort);

        assertThat(result).isEqualTo("createdDate,desc");
    }
}