package com.example.app.controller;

import com.example.model.Album;
import com.example.service.AlbumService;
import com.example.service.ArtistService;
import com.example.service.BandService;
import com.example.service.SongService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumControllerTest {

    @Mock
    private AlbumService albumService;

    @Mock
    private BandService bandService;

    @Mock
    private SongService songService;

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private AlbumController albumController;

    @Test
    void albums_validPageable_returnsAlbumsViewWithPageInModel() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "releaseDate"));
        Page<Album> albumPage = new PageImpl<>(List.of(new Album()));
        ModelMap modelMap = new ModelMap();

        when(albumService.findAlbumPage(pageable)).thenReturn(albumPage);

        String view = albumController.albums(modelMap, pageable);

        assertEquals("albums", view);
        assertEquals(albumPage, modelMap.get("albums"));
        verify(albumService).findAlbumPage(pageable);
    }

    @Test
    void albums_emptyPage_returnsAlbumsViewWithEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "releaseDate"));
        Page<Album> emptyPage = new PageImpl<>(Collections.emptyList());
        ModelMap modelMap = new ModelMap();

        when(albumService.findAlbumPage(pageable)).thenReturn(emptyPage);

        String view = albumController.albums(modelMap, pageable);

        assertEquals("albums", view);
        assertEquals(emptyPage, modelMap.get("albums"));
    }

    @Test
    void preview_validId_returnsAlbumPreviewViewWithAlbumAndSongs() {
        Long id = 1L;
        Album album = new Album();
        List<Object> songs = List.of(new Object(), new Object());
        ModelMap modelMap = new ModelMap();

        when(albumService.findAlbumById(id)).thenReturn(album);
        doReturn(songs).when(songService).getSongsByAlbumId(id);

        String view = albumController.preview(modelMap, id);

        assertEquals("albumPreview", view);
        assertEquals(album, modelMap.get("album"));
        assertEquals(songs, modelMap.get("songs"));
        verify(albumService).findAlbumById(id);
        verify(songService).getSongsByAlbumId(id);
    }

    @Test
    void preview_noSongsForAlbum_modelContainsEmptySongList() {
        Long id = 2L;
        Album album = new Album();
        ModelMap modelMap = new ModelMap();

        when(albumService.findAlbumById(id)).thenReturn(album);
        doReturn(Collections.emptyList()).when(songService).getSongsByAlbumId(id);

        String view = albumController.preview(modelMap, id);

        assertEquals("albumPreview", view);
        assertEquals(Collections.emptyList(), modelMap.get("songs"));
    }

    @Test
    void preview_bothServicesCalledWithSameId() {
        Long id = 7L;
        ModelMap modelMap = new ModelMap();

        when(albumService.findAlbumById(id)).thenReturn(new Album());
        doReturn(Collections.emptyList()).when(songService).getSongsByAlbumId(id);

        albumController.preview(modelMap, id);

        verify(albumService).findAlbumById(eq(7L));
        verify(songService).getSongsByAlbumId(eq(7L));
    }

    @Test
    void addAlbum_withBandNameAndArtistName_returnsAddAlbumViewWithModel() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();
        String bandName = "Beatles";
        String artistName = "Lennon";
        Page<Object> bands = new PageImpl<>(List.of(new Object()));
        Page<Object> artists = new PageImpl<>(List.of(new Object()));

        doReturn(bands).when(bandService).getBandsByName(bandName, pageable);
        doReturn(artists).when(artistService).getArtistsByName(artistName, pageable);

        String view = albumController.addAlbum(modelMap, pageable, bandName, artistName);

        assertEquals("addAlbum", view);
        assertEquals(bands, modelMap.get("bands"));
        assertEquals(artists, modelMap.get("artists"));
        assertNotNull(modelMap.get("now"));
        verify(bandService).getBandsByName(bandName, pageable);
        verify(artistService).getArtistsByName(artistName, pageable);
    }

    @Test
    void addAlbum_withNullBandNameAndArtistName_passesNullsToServices() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();

        doReturn(new PageImpl<>(Collections.emptyList())).when(bandService).getBandsByName(null, pageable);
        doReturn(new PageImpl<>(Collections.emptyList())).when(artistService).getArtistsByName(null, pageable);

        String view = albumController.addAlbum(modelMap, pageable, null, null);

        assertEquals("addAlbum", view);
        verify(bandService).getBandsByName(isNull(), eq(pageable));
        verify(artistService).getArtistsByName(isNull(), eq(pageable));
    }

    @Test
    void addAlbum_nowAttributeIsLocalDateTime() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();
        LocalDateTime before = LocalDateTime.now();

        doReturn(new PageImpl<>(Collections.emptyList())).when(bandService).getBandsByName(null, pageable);
        doReturn(new PageImpl<>(Collections.emptyList())).when(artistService).getArtistsByName(null, pageable);

        albumController.addAlbum(modelMap, pageable, null, null);
        LocalDateTime after = LocalDateTime.now();

        LocalDateTime now = (LocalDateTime) modelMap.get("now");
        assertNotNull(now);
        assertTrue(!now.isBefore(before) && !now.isAfter(after));
    }

    @Test
    void deleteAlbum_validId_redirectsToAlbums() {
        Long id = 1L;

        String view = albumController.deleteAlbum(id);

        assertEquals("redirect:/albums", view);
        verify(albumService).delete(id);
    }

    @Test
    void deleteAlbum_serviceCalledWithCorrectId() {
        Long id = 99L;

        albumController.deleteAlbum(id);

        verify(albumService).delete(eq(99L));
    }

    @Test
    void updateAlbum_validId_returnsEditAlbumViewWithAllModelAttributes() {
        Long id = 1L;
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();
        String bandName = "Radiohead";
        String artistName = "Yorke";
        Album album = new Album();
        Page<Object> bands = new PageImpl<>(List.of(new Object()));
        Page<Object> artists = new PageImpl<>(List.of(new Object()));

        doReturn(bands).when(bandService).getBandsByName(bandName, pageable);
        doReturn(artists).when(artistService).getArtistsByName(artistName, pageable);
        when(albumService.findAlbumById(id)).thenReturn(album);

        String view = albumController.updateAlbum(modelMap, pageable, bandName, artistName, id);

        assertEquals("editAlbum", view);
        assertEquals(bands, modelMap.get("bands"));
        assertEquals(artists, modelMap.get("artists"));
        assertEquals(album, modelMap.get("album"));
        assertNotNull(modelMap.get("now"));
        verify(bandService).getBandsByName(bandName, pageable);
        verify(artistService).getArtistsByName(artistName, pageable);
        verify(albumService).findAlbumById(id);
    }

    @Test
    void updateAlbum_withNullBandNameAndArtistName_passesNullsToServices() {
        Long id = 3L;
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();

        doReturn(new PageImpl<>(Collections.emptyList())).when(bandService).getBandsByName(null, pageable);
        doReturn(new PageImpl<>(Collections.emptyList())).when(artistService).getArtistsByName(null, pageable);
        when(albumService.findAlbumById(id)).thenReturn(new Album());

        String view = albumController.updateAlbum(modelMap, pageable, null, null, id);

        assertEquals("editAlbum", view);
        verify(bandService).getBandsByName(isNull(), eq(pageable));
        verify(artistService).getArtistsByName(isNull(), eq(pageable));
    }

    @Test
    void updateAlbum_nowAttributeIsLocalDateTime() {
        Long id = 5L;
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();
        LocalDateTime before = LocalDateTime.now();

        doReturn(new PageImpl<>(Collections.emptyList())).when(bandService).getBandsByName(null, pageable);
        doReturn(new PageImpl<>(Collections.emptyList())).when(artistService).getArtistsByName(null, pageable);
        when(albumService.findAlbumById(id)).thenReturn(new Album());

        albumController.updateAlbum(modelMap, pageable, null, null, id);
        LocalDateTime after = LocalDateTime.now();

        LocalDateTime now = (LocalDateTime) modelMap.get("now");
        assertNotNull(now);
        assertTrue(!now.isBefore(before) && !now.isAfter(after));
    }

    @Test
    void updateAlbum_albumServiceCalledWithCorrectId() {
        Long id = 42L;
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));
        ModelMap modelMap = new ModelMap();

        doReturn(new PageImpl<>(Collections.emptyList())).when(bandService).getBandsByName(null, pageable);
        doReturn(new PageImpl<>(Collections.emptyList())).when(artistService).getArtistsByName(null, pageable);
        when(albumService.findAlbumById(id)).thenReturn(new Album());

        albumController.updateAlbum(modelMap, pageable, null, null, id);

        verify(albumService).findAlbumById(eq(42L));
    }

    @Test
    void addAlbum_post_withBandIdAndArtistId_redirectsToAlbums() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Long bandId = 1L;
        Long artistId = 2L;

        String view = albumController.addAlbum(album, multipartFile, bandId, artistId);

        assertEquals("redirect:/albums", view);
        verify(albumService).save(album, multipartFile, bandId, artistId);
    }

    @Test
    void addAlbum_post_withNullBandIdAndArtistId_stillCallsServiceAndRedirects() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);

        String view = albumController.addAlbum(album, multipartFile, null, null);

        assertEquals("redirect:/albums", view);
        verify(albumService).save(eq(album), eq(multipartFile), isNull(), isNull());
    }

    @Test
    void addAlbum_post_serviceCalledWithCorrectArguments() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Long bandId = 10L;
        Long artistId = 20L;

        albumController.addAlbum(album, multipartFile, bandId, artistId);

        verify(albumService).save(eq(album), eq(multipartFile), eq(10L), eq(20L));
    }

    @Test
    void addAlbum_post_withOnlyBandId_artistIdIsNull() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Long bandId = 5L;

        String view = albumController.addAlbum(album, multipartFile, bandId, null);

        assertEquals("redirect:/albums", view);
        verify(albumService).save(eq(album), eq(multipartFile), eq(5L), isNull());
    }

    @Test
    void updateAlbum_post_withBandIdAndArtistId_redirectsToAlbums() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Long bandId = 3L;
        Long artistId = 4L;

        String view = albumController.updateAlbum(album, multipartFile, bandId, artistId);

        assertEquals("redirect:/albums", view);
        verify(albumService).update(album, multipartFile, bandId, artistId);
    }

    @Test
    void updateAlbum_post_withNullBandIdAndArtistId_stillCallsServiceAndRedirects() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);

        String view = albumController.updateAlbum(album, multipartFile, null, null);

        assertEquals("redirect:/albums", view);
        verify(albumService).update(eq(album), eq(multipartFile), isNull(), isNull());
    }

    @Test
    void updateAlbum_post_serviceCalledWithCorrectArguments() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Long bandId = 11L;
        Long artistId = 22L;

        albumController.updateAlbum(album, multipartFile, bandId, artistId);

        verify(albumService).update(eq(album), eq(multipartFile), eq(11L), eq(22L));
    }

    @Test
    void updateAlbum_post_withOnlyArtistId_bandIdIsNull() {
        Album album = new Album();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Long artistId = 8L;

        String view = albumController.updateAlbum(album, multipartFile, null, artistId);

        assertEquals("redirect:/albums", view);
        verify(albumService).update(eq(album), eq(multipartFile), isNull(), eq(8L));
    }
}