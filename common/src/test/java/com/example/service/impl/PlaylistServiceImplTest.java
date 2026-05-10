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

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private PlaylistMapper playlistMapper;
    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    @Test
    void findAll_happyPath_returnsMappedDtoList() {
        Sort sort = Sort.by("name");
        Playlist playlist = new Playlist();
        PlaylistDto dto = new PlaylistDto();
        when(playlistRepository.findAll(sort)).thenReturn(List.of(playlist));
        when(playlistMapper.toDto(playlist)).thenReturn(dto);

        List<PlaylistDto> result = playlistService.findAll(sort);

        assertThat(result).containsExactly(dto);
        verify(playlistMapper).toDto(playlist);
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        Sort sort = Sort.unsorted();
        when(playlistRepository.findAll(sort)).thenReturn(List.of());

        List<PlaylistDto> result = playlistService.findAll(sort);

        assertThat(result).isEmpty();
        verify(playlistMapper, never()).toDto(any());
    }

    @Test
    void create_withFileAndSongIds_uploadsImageAndSetsSongsOnEntity() {
        PlaylistDto playlistDto = new PlaylistDto();
        Playlist playlist = new Playlist();
        Playlist saved = new Playlist();
        PlaylistDto savedDto = new PlaylistDto();
        Song song = new Song();
        MultipartFile file = mock(MultipartFile.class);
        User user = new User();

        when(file.isEmpty()).thenReturn(false);
        when(playlistMapper.toEntity(playlistDto)).thenReturn(playlist);
        when(storageService.upload(file, "playlist-images")).thenReturn("http://img.url");
        when(songRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(song));
        when(playlistRepository.save(playlist)).thenReturn(saved);
        when(playlistMapper.toDto(saved)).thenReturn(savedDto);

        PlaylistDto result = playlistService.create(playlistDto, file, List.of(1L, 2L), user);

        assertThat(playlist.getPictureUrl()).isEqualTo("http://img.url");
        assertThat(playlist.getSongs()).containsExactly(song);
        assertThat(playlist.getUser()).isEqualTo(user);
        assertThat(result).isEqualTo(savedDto);
    }

    @Test
    void create_withNullFile_usesDefaultImageAndDoesNotUpload() {
        PlaylistDto playlistDto = new PlaylistDto();
        Playlist playlist = new Playlist();
        User user = new User();

        when(playlistMapper.toEntity(playlistDto)).thenReturn(playlist);
        when(songRepository.findAllById(List.of())).thenReturn(List.of());
        when(playlistRepository.save(playlist)).thenReturn(playlist);
        when(playlistMapper.toDto(playlist)).thenReturn(new PlaylistDto());

        playlistService.create(playlistDto, null, null, user);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void create_withEmptyFile_usesDefaultImageAndDoesNotUpload() {
        PlaylistDto playlistDto = new PlaylistDto();
        Playlist playlist = new Playlist();
        MultipartFile file = mock(MultipartFile.class);
        User user = new User();

        when(file.isEmpty()).thenReturn(true);
        when(playlistMapper.toEntity(playlistDto)).thenReturn(playlist);
        when(songRepository.findAllById(List.of())).thenReturn(List.of());
        when(playlistRepository.save(playlist)).thenReturn(playlist);
        when(playlistMapper.toDto(playlist)).thenReturn(new PlaylistDto());

        playlistService.create(playlistDto, file, null, user);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void create_withNullSongIds_usesEmptyListForSongLookup() {
        PlaylistDto playlistDto = new PlaylistDto();
        Playlist playlist = new Playlist();
        User user = new User();

        when(playlistMapper.toEntity(playlistDto)).thenReturn(playlist);
        when(songRepository.findAllById(List.of())).thenReturn(List.of());
        when(playlistRepository.save(playlist)).thenReturn(playlist);
        when(playlistMapper.toDto(playlist)).thenReturn(new PlaylistDto());

        playlistService.create(playlistDto, null, null, user);

        verify(songRepository).findAllById(List.of());
    }

    @Test
    void update_withFile_updatesFieldsAndUploadsImage() {
        PlaylistDto playlistDto = PlaylistDto.builder().name("New Name").publicFlag(true).build();
        Playlist existing = new Playlist();
        MultipartFile file = mock(MultipartFile.class);
        PlaylistDto updatedDto = new PlaylistDto();

        when(file.isEmpty()).thenReturn(false);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storageService.upload(file, "playlist-images")).thenReturn("http://new-img.url");
        when(playlistRepository.save(existing)).thenReturn(existing);
        when(playlistMapper.toDto(existing)).thenReturn(updatedDto);

        PlaylistDto result = playlistService.update(1L, playlistDto, file);

        verify(playlistMapper).updateEntityFromDto(playlistDto, existing);
        assertThat(existing.getPictureUrl()).isEqualTo("http://new-img.url");
        assertThat(result).isEqualTo(updatedDto);
    }

    @Test
    void update_withNullFile_doesNotUploadImage() {
        PlaylistDto playlistDto = new PlaylistDto();
        Playlist existing = new Playlist();

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(playlistRepository.save(existing)).thenReturn(existing);
        when(playlistMapper.toDto(existing)).thenReturn(new PlaylistDto());

        playlistService.update(1L, playlistDto, null);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void update_playlistNotFound_throwsEntityNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.update(99L, new PlaylistDto(), null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_nonDefaultPlaylist_deletesById() {
        Playlist playlist = new Playlist();
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        playlistService.delete(1L);

        verify(playlistRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
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
    void setVisibility_happyPath_updatesPublicFlagAndFlushes() {
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
        when(playlistRepository.findByIdWithSongs(99L)).thenReturn(Optional.empty());

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
        when(playlistRepository.findByIdWithSongs(99L)).thenReturn(Optional.empty());

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