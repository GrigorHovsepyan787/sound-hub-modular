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
        verify(songRepository).findAll(pageable);
        verify(songMapper).toDto(song);
    }

    @Test
    void findAll_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(songRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<SongDto> result = songService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        verify(songMapper, never()).toDto(any());
    }

    @Test
    void save_happyPath_uploadsFileAndPersistsMappedEntity() throws Exception {
        SongDto dto = SongDto.builder()
                .title("Test Song")
                .artistId(1L)
                .bandId(2L)
                .albumId(3L)
                .build();
        Song entity = new Song();
        Song saved = new Song();
        SongDto savedDto = new SongDto();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test.mp3");
        doThrow(new IOException("no real file")).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("http://cdn/song.mp3");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(saved);
        when(songMapper.toDto(saved)).thenReturn(savedDto);

        SongDto result = songService.save(dto, file);

        assertThat(result).isEqualTo(savedDto);
        assertThat(entity.getSongUrl()).isEqualTo("http://cdn/song.mp3");
        assertThat(entity.getDuration()).isEqualTo(0); // IOException forces fallback
        verify(storageService).upload(file, "songs");
        verify(songMapper).toEntity(dto);
        verify(songRepository).save(entity);
        verify(songMapper).toDto(saved);
    }

    @Test
    void save_withArtistId_setsArtistOnEntity() throws Exception {
        SongDto dto = SongDto.builder().artistId(10L).build();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getArtist()).isNotNull();
        assertThat(entity.getArtist().getId()).isEqualTo(10L);
    }

    @Test
    void save_withNullArtistId_doesNotSetArtist() throws Exception {
        SongDto dto = SongDto.builder().artistId(null).albumId(1L).build();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getArtist()).isNull();
    }

    @Test
    void save_withBandId_setsBandOnEntity() throws Exception {
        SongDto dto = SongDto.builder().bandId(20L).build();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getBand()).isNotNull();
        assertThat(entity.getBand().getId()).isEqualTo(20L);
    }

    @Test
    void save_withNullBandId_doesNotSetBand() throws Exception {
        SongDto dto = SongDto.builder().bandId(null).albumId(1L).build();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getBand()).isNull();
    }

    @Test
    void save_withAlbumId_setsAlbumOnEntity() throws Exception {
        SongDto dto = SongDto.builder().albumId(30L).build();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getAlbum()).isNotNull();
        assertThat(entity.getAlbum().getId()).isEqualTo(30L);
    }

    @Test
    void save_withNullAlbumId_doesNotSetAlbum() throws Exception {
        SongDto dto = SongDto.builder().albumId(null).build();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getAlbum()).isNull();
    }

    @Test
    void save_storageUploadCalledWithCorrectBucket() throws Exception {
        SongDto dto = new SongDto();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        verify(storageService).upload(eq(file), eq("songs"));
    }

    @Test
    void save_duractionExtractionFails_defaultsToZero() throws Exception {
        SongDto dto = new SongDto();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("bad.mp3");
        doThrow(new IOException("disk error")).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("url");
        when(songMapper.toEntity(dto)).thenReturn(entity);
        when(songRepository.save(entity)).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(entity.getDuration()).isEqualTo(0);
    }

    @Test
    void save_songUrlSetOnEntityBeforePersist() throws Exception {
        SongDto dto = new SongDto();
        Song entity = new Song();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("track.mp3");
        doThrow(new IOException()).when(file).transferTo(any(File.class));
        when(storageService.upload(file, "songs")).thenReturn("https://cdn/song.mp3");
        when(songMapper.toEntity(dto)).thenReturn(entity);

        ArgumentCaptor<Song> savedCaptor = ArgumentCaptor.forClass(Song.class);
        when(songRepository.save(savedCaptor.capture())).thenReturn(entity);
        when(songMapper.toDto(entity)).thenReturn(new SongDto());

        songService.save(dto, file);

        assertThat(savedCaptor.getValue().getSongUrl()).isEqualTo("https://cdn/song.mp3");
    }

    @Test
    void delete_happyPath_deletesSongPlaysAndSong() {
        songService.delete(1L);

        verify(songPlayRepository).deleteBySongId(1L);
        verify(songRepository).deleteById(1L);
    }

    @Test
    void delete_playRepositoryCalledBeforeSongRepository() {
        // Verifies ordering: plays must go first to avoid FK violation
        var order = org.mockito.Mockito.inOrder(songPlayRepository, songRepository);

        songService.delete(5L);

        order.verify(songPlayRepository).deleteBySongId(5L);
        order.verify(songRepository).deleteById(5L);
    }

    @Test
    void getSongById_exists_returnsMappedDto() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        SongDto result = songService.getSongById(1L);

        assertThat(result).isEqualTo(dto);
        verify(songMapper).toDto(song);
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
    void getPageNumbers_withOnePage_returnsSingleElement() {
        Page<SongDto> page = mock(Page.class);
        when(page.getTotalPages()).thenReturn(1);

        List<Integer> result = songService.getPageNumbers(page);

        assertThat(result).containsExactly(1);
    }

    @Test
    void findSongsByGenre_withGenre_filtersByGenre() {
        Pageable pageable = PageRequest.of(0, 5);
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findByGenre(Genre.ROCK, pageable)).thenReturn(new PageImpl<>(List.of(song)));
        when(songMapper.toDto(song)).thenReturn(dto);

        Page<SongDto> result = songService.findSongsByGenre(Genre.ROCK, pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(songRepository).findByGenre(Genre.ROCK, pageable);
        verify(songRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void findSongsByGenre_withNullGenre_returnsAllSongs() {
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

        verify(songPlayRepository, never()).save(any());
        verify(songRepository, never()).incrementPlayCount(any());
    }

    @Test
    void registerPlay_playedAtIsSet() {
        Song song = new Song();
        song.setId(1L);
        when(songRepository.findById(1L)).thenReturn(Optional.of(song));

        songService.registerPlay(1L);

        ArgumentCaptor<SongPlay> captor = ArgumentCaptor.forClass(SongPlay.class);
        verify(songPlayRepository).save(captor.capture());
        assertThat(captor.getValue().getPlayedAt()).isNotNull();
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
        verify(songPopularityMapper).toDto(popularity);
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
    void getTop5SongsOfArtistByPlayCount_noSongs_returnsEmptyList() {
        when(songRepository.findTop5ByArtistIdOrderByPlayCountDesc(999L)).thenReturn(List.of());

        List<SongDto> result = songService.getTop5SongsOfArtistByPlayCount(999L);

        assertThat(result).isEmpty();
        verify(songMapper, never()).toDto(any());
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
    void getTop5SongsOfBandByPlayCount_noSongs_returnsEmptyList() {
        when(songRepository.findTop5ByBandIdOrderByPlayCountDesc(999L)).thenReturn(List.of());

        List<SongDto> result = songService.getTop5SongsOfBandByPlayCount(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByPlayCount_happyPath_returnsMappedList() {
        Song song = new Song();
        SongDto dto = new SongDto();
        when(songRepository.findTopByPlayCount(PageRequest.of(0, 5))).thenReturn(List.of(song));
        when(songMapper.toDto(song)).thenReturn(dto);

        List<SongDto> result = songService.findTopByPlayCount(5);

        assertThat(result).containsExactly(dto);
        verify(songRepository).findTopByPlayCount(PageRequest.of(0, 5));
    }

    @Test
    void findTopByPlayCount_usesCorrectPageRequest() {
        when(songRepository.findTopByPlayCount(PageRequest.of(0, 3))).thenReturn(List.of());

        songService.findTopByPlayCount(3);

        verify(songRepository).findTopByPlayCount(PageRequest.of(0, 3));
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
    void getSongsByAlbumId_noSongs_returnsEmptyList() {
        when(songRepository.findByAlbumId(99L)).thenReturn(List.of());

        List<SongDto> result = songService.getSongsByAlbumId(99L);

        assertThat(result).isEmpty();
        verify(songMapper, never()).toDto(any());
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
        verify(songRepository).findByTitleContainingIgnoreCase("rock", PageRequest.of(0, 10));
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

    @Test
    void searchSongs_queryIsCaseInsensitive() {
        when(songRepository.findByTitleContainingIgnoreCase("ROCK", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        songService.searchSongs("ROCK", 10);

        verify(songRepository).findByTitleContainingIgnoreCase(eq("ROCK"), any());
    }

    @Test
    void searchSongs_limitsResultsCorrectly() {
        when(songRepository.findByTitleContainingIgnoreCase("jazz", PageRequest.of(0, 3)))
                .thenReturn(new PageImpl<>(List.of()));

        songService.searchSongs("jazz", 3);

        verify(songRepository).findByTitleContainingIgnoreCase(any(), eq(PageRequest.of(0, 3)));
    }
}