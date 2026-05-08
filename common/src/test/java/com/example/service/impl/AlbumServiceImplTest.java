package com.example.service.impl;

import com.example.model.Album;
import com.example.model.Artist;
import com.example.model.Band;
import com.example.projection.AlbumPopularity;
import com.example.repository.AlbumRepository;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceImplTest {

    @Mock private AlbumRepository albumRepository;
    @Mock private StorageService storageService;
    @Mock private BandRepository bandRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private SongPlayRepository songPlayRepository;

    @InjectMocks private AlbumServiceImpl albumService;

    @Test
    void findAlbumPage_happyPath_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expected = new PageImpl<>(List.of(new Album()));
        when(albumRepository.findAll(pageable)).thenReturn(expected);

        Page<Album> result = albumService.findAlbumPage(pageable);

        assertThat(result).isEqualTo(expected);
        verify(albumRepository).findAll(pageable);
    }

    @Test
    void findAll_happyPath_returnsList() {
        List<Album> albums = List.of(new Album(), new Album());
        when(albumRepository.findAll()).thenReturn(albums);

        List<Album> result = albumService.findAll();

        assertThat(result).hasSize(2);
        verify(albumRepository).findAll();
    }

    @Test
    void save_withBandIdAndFile_uploadsImageAndSetsBand() {
        Album album = new Album();
        MultipartFile file = mock(MultipartFile.class);
        Band band = new Band();
        when(file.isEmpty()).thenReturn(false);
        when(storageService.upload(file, "album-images")).thenReturn("http://img.url/album.jpg");
        when(bandRepository.findById(1L)).thenReturn(Optional.of(band));

        albumService.save(album, file, 1L, null);

        assertThat(album.getPictureUrl()).isEqualTo("http://img.url/album.jpg");
        assertThat(album.getBand()).isEqualTo(band);
        verify(albumRepository).save(album);
    }

    @Test
    void save_withArtistIdAndNullFile_setsDefaultImageAndArtist() {
        Album album = new Album();
        Artist artist = new Artist();
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));

        albumService.save(album, null, null, 2L);

        assertThat(album.getArtist()).isEqualTo(artist);
        verify(albumRepository).save(album);
    }

    @Test
    void save_bothBandIdAndArtistIdNull_throwsIllegalArgumentException() {
        Album album = new Album();

        assertThatThrownBy(() -> albumService.save(album, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bandId");
    }

    @Test
    void save_bandNotFound_throwsEntityNotFoundException() {
        Album album = new Album();
        when(bandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.save(album, null, 99L, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void save_artistNotFound_throwsEntityNotFoundException() {
        Album album = new Album();
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.save(album, null, null, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void save_fileUploadReturnsNull_doesNotSetPictureUrl() {
        Album album = new Album();
        MultipartFile file = mock(MultipartFile.class);
        Band band = new Band();
        when(file.isEmpty()).thenReturn(false);
        when(storageService.upload(file, "album-images")).thenReturn(null);
        when(bandRepository.findById(1L)).thenReturn(Optional.of(band));

        albumService.save(album, file, 1L, null);

        assertThat(album.getPictureUrl()).isNull();
        verify(albumRepository).save(album);
    }

    @Test
    void findAlbumById_exists_returnsAlbum() {
        Album album = new Album();
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        Album result = albumService.findAlbumById(1L);

        assertThat(result).isEqualTo(album);
    }

    @Test
    void findAlbumById_notFound_throwsEntityNotFoundException() {
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.findAlbumById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_withBandId_updatesBandAndClearsArtist() {
        Album existing = new Album();
        existing.setId(1L);
        Album update = new Album();
        update.setId(1L);
        update.setTitle("New Title");
        update.setReleaseDate(LocalDateTime.now());
        Band band = new Band();
        when(albumRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bandRepository.findById(5L)).thenReturn(Optional.of(band));

        albumService.update(update, null, 5L, null);

        assertThat(existing.getBand()).isEqualTo(band);
        assertThat(existing.getArtist()).isNull();
        assertThat(existing.getTitle()).isEqualTo("New Title");
        verify(albumRepository).save(existing);
    }

    @Test
    void update_withArtistId_updatesArtistAndClearsBand() {
        Album existing = new Album();
        existing.setId(1L);
        Album update = new Album();
        update.setId(1L);
        update.setTitle("Artist Album");
        Artist artist = new Artist();
        when(albumRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(artistRepository.findById(3L)).thenReturn(Optional.of(artist));

        albumService.update(update, null, null, 3L);

        assertThat(existing.getArtist()).isEqualTo(artist);
        assertThat(existing.getBand()).isNull();
    }

    @Test
    void update_withMultipartFile_uploadsAndSetsImage() {
        Album existing = new Album();
        existing.setId(1L);
        Album update = new Album();
        update.setId(1L);
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storageService.upload(file, "album-images")).thenReturn("http://new-img.url");

        albumService.update(update, file, null, null);

        assertThat(existing.getPictureUrl()).isEqualTo("http://new-img.url");
    }

    @Test
    void update_albumNotFound_throwsEntityNotFoundException() {
        Album update = new Album();
        update.setId(99L);
        when(albumRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.update(update, null, null, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTopAlbumPopularityLastMonth_happyPath_returnsSongPlayData() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<AlbumPopularity> expected = new PageImpl<>(List.of());
        when(songPlayRepository.findTopAlbumsForPeriod(any(), any(), eq(pageable))).thenReturn(expected);

        Page<AlbumPopularity> result = albumService.getTopAlbumPopularityLastMonth(pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByArtistIsNotNull_happyPath_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expected = new PageImpl<>(List.of(new Album()));
        when(albumRepository.findByArtistIsNotNull(pageable)).thenReturn(expected);

        Page<Album> result = albumService.findByArtistIsNotNull(pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByBandIsNotNull_happyPath_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expected = new PageImpl<>(List.of(new Album()));
        when(albumRepository.findByBandIsNotNull(pageable)).thenReturn(expected);

        Page<Album> result = albumService.findByBandIsNotNull(pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void delete_happyPath_deletesById() {
        albumService.delete(1L);

        verify(albumRepository).deleteById(1L);
    }
}