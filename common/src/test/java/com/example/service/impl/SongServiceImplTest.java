package com.example.service.impl;

import com.example.dto.SongDto;
import com.example.dto.SongPopularityDto;
import com.example.mapper.SongMapper;
import com.example.mapper.SongPopularityMapper;
import com.example.model.Genre;
import com.example.model.Song;
import com.example.model.SongPlay;
import com.example.projection.SongPopularity;
import com.example.repository.SongPlayRepository;
import com.example.repository.SongRepository;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongServiceImplTest {

    @Mock private SongRepository songRepository;
    @Mock private SongMapper songMapper;
    @Mock private SongPopularityMapper songPopularityMapper;
    @Mock private StorageService storageService;
    @Mock private SongPlayRepository songPlayRepository;

    @InjectMocks private SongServiceImpl songService;

    @Test
    void findAll_happyPath_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(song)));
        when(songMapper.toDto(song)).thenReturn(dto);

        Page<SongDto> result = songService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void save_happyPath_uploadsFileAndSaves() throws Exception {
        Song song = new Song();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.mp3");
        doThrow(new IOException("test")).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("http://song.url");
        when(songRepository.save(song)).thenReturn(song);

        Song result = songService.save(song, file);

        assertThat(song.getSongUrl()).isEqualTo("http://song.url");
        assertThat(song.getDuration()).isEqualTo(0);
        assertThat(result).isEqualTo(song);
        verify(storageService).upload(file, "songs");
        verify(songRepository).save(song);
    }

    @Test
    void delete_happyPath_deletesSongPlaysAndSong() {
        songService.delete(1L);

        verify(songPlayRepository).deleteBySongId(1L);
        verify(songRepository).deleteById(1L);
    }

    @Test
    void getSongById_exists_returnsSong() {
        Song song = new Song();
        when(songRepository.findById(1L)).thenReturn(Optional.of(song));

        Song result = songService.getSongById(1L);

        assertThat(result).isEqualTo(song);
    }

    @Test
    void getSongById_notFound_throwsEntityNotFoundException() {
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> songService.getSongById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPageNumbers_withPages_returnsRange() {
        Page<SongDto> page = mock(Page.class);
        when(page.getTotalPages()).thenReturn(3);

        List<Integer> result = songService.getPageNumbers(page);

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void getPageNumbers_withZeroTotalPages_returnsEmptyList() {
        Page<SongDto> page = mock(Page.class);
        when(page.getTotalPages()).thenReturn(0);

        List<Integer> result = songService.getPageNumbers(page);

        assertThat(result).isEmpty();
    }

    @Test
    void findByGenre_happyPath_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findByGenre(Genre.ROCK, pageable)).thenReturn(new PageImpl<>(List.of(song)));
        when(songMapper.toDto(song)).thenReturn(dto);

        Page<SongDto> result = songService.findByGenre(Genre.ROCK, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void findSongsByGenre_withGenre_filtersByGenre() {
        Pageable pageable = PageRequest.of(0, 5);
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findByGenre(Genre.POP, pageable)).thenReturn(new PageImpl<>(List.of(song)));
        when(songMapper.toDto(song)).thenReturn(dto);

        Page<SongDto> result = songService.findSongsByGenre(Genre.POP, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void findSongsByGenre_withNullGenre_returnsAll() {
        Pageable pageable = PageRequest.of(0, 5);
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(song)));
        when(songMapper.toDto(song)).thenReturn(dto);

        Page<SongDto> result = songService.findSongsByGenre(null, pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(songRepository, never()).findByGenre(any(), any());
    }

    @Test
    void registerPlay_happyPath_createsSongPlayAndIncrementsCount() {
        Song song = new Song();
        song.setId(1L);
        when(songRepository.findById(1L)).thenReturn(Optional.of(song));

        songService.registerPlay(1L);

        ArgumentCaptor<SongPlay> captor = ArgumentCaptor.forClass(SongPlay.class);
        verify(songPlayRepository).save(captor.capture());
        assertThat(captor.getValue().getSong()).isEqualTo(song);
        assertThat(captor.getValue().getPlayedAt()).isNotNull();
        verify(songRepository).incrementPlayCount(1L);
    }

    @Test
    void registerPlay_songNotFound_throwsEntityNotFoundException() {
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> songService.registerPlay(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTopSongPopularityLastMonth_happyPath_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        SongPopularity popularity = mock(SongPopularity.class);
        SongPopularityDto dto = new SongPopularityDto();
        when(songPlayRepository.findTopSongsForPeriod(any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(popularity)));
        when(songPopularityMapper.toDto(popularity)).thenReturn(dto);

        Page<SongPopularityDto> result = songService.getTopSongPopularityLastMonth(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void getTop5SongsOfArtistByPlayCount_happyPath_returnsMappedList() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findTop5ByArtistIdOrderByPlayCountDesc(1L)).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.getTop5SongsOfArtistByPlayCount(1L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getTop5SongsOfBandByPlayCount_happyPath_returnsMappedList() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findTop5ByBandIdOrderByPlayCountDesc(2L)).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.getTop5SongsOfBandByPlayCount(2L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void findTopByPlayCount_happyPath_returnsMappedList() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findTopByPlayCount(PageRequest.of(0, 5))).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.findTopByPlayCount(5);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getSongsByAlbumId_happyPath_returnsMappedList() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findByAlbumId(1L)).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.getSongsByAlbumId(1L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void searchSongs_withValidQuery_searchesByTitle() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findByTitleContainingIgnoreCase("rock", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(song)));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.searchSongs("rock", 10);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void searchSongs_withNullQuery_fallsBackToTopByPlayCount() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findTopByPlayCount(PageRequest.of(0, 5))).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.searchSongs(null, 5);

        assertThat(result).containsExactly(dto);
        verify(songRepository, never()).findByTitleContainingIgnoreCase(any(), any());
    }

    @Test
    void searchSongs_withBlankQuery_fallsBackToTopByPlayCount() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findTopByPlayCount(PageRequest.of(0, 5))).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.searchSongs("   ", 5);

        assertThat(result).containsExactly(dto);
        verify(songRepository, never()).findByTitleContainingIgnoreCase(any(), any());
    }
}