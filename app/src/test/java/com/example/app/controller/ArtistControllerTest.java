package com.example.app.controller;

import com.example.dto.ArtistDto;
import com.example.dto.BandDto;
import com.example.dto.SongDto;
import com.example.model.Artist;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistService artistService;

    @Mock
    private BandService bandService;

    @Mock
    private SongService songService;

    @InjectMocks
    private ArtistController artistController;

    @Test
    void artists_withMultiplePages_returnsArtistsViewAndCorrectPageNumbers() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"));
        List<ArtistDto> artistList = List.of(new ArtistDto(), new ArtistDto());
        Page<ArtistDto> artistPage = new PageImpl<>(artistList, pageable, 12);
        ModelMap modelMap = new ModelMap();

        when(artistService.findAll(pageable)).thenReturn(artistPage);
        when(artistService.getPageNumbers(artistPage)).thenReturn(List.of(1, 2));

        String view = artistController.artists(modelMap, pageable);

        assertEquals("artists", view);
        assertEquals(artistPage, modelMap.get("artists"));

        @SuppressWarnings("unchecked")
        List<Integer> pageNumbers = (List<Integer>) modelMap.get("pageNumbers");

        assertEquals(List.of(1, 2), pageNumbers);

        verify(artistService).findAll(pageable);
        verify(artistService).getPageNumbers(artistPage);
    }

    @Test
    void artists_withSinglePage_returnsPageNumberListOfOne() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"));
        Page<ArtistDto> artistPage = new PageImpl<>(List.of(new ArtistDto()), pageable, 1);
        ModelMap modelMap = new ModelMap();

        when(artistService.findAll(pageable)).thenReturn(artistPage);
        when(artistService.getPageNumbers(artistPage)).thenReturn(List.of(1));

        artistController.artists(modelMap, pageable);

        @SuppressWarnings("unchecked")
        List<Integer> pageNumbers = (List<Integer>) modelMap.get("pageNumbers");

        assertEquals(List.of(1), pageNumbers);

        verify(artistService).getPageNumbers(artistPage);
    }

    @Test
    void artists_withZeroTotalPages_returnsEmptyPageNumbers() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"));
        Page<ArtistDto> artistPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        ModelMap modelMap = new ModelMap();

        when(artistService.findAll(pageable)).thenReturn(artistPage);

        artistController.artists(modelMap, pageable);

        @SuppressWarnings("unchecked")
        List<Integer> pageNumbers = (List<Integer>) modelMap.get("pageNumbers");
        assertEquals(Collections.emptyList(), pageNumbers);
    }

    @Test
    void artists_withSortByName_currentSortAttributePopulatedCorrectly() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "name"));
        Page<ArtistDto> artistPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        ModelMap modelMap = new ModelMap();

        when(artistService.findAll(pageable)).thenReturn(artistPage);

        artistController.artists(modelMap, pageable);

        assertEquals("name,asc", modelMap.get("currentSort"));
    }

    @Test
    void artists_withUnsortedPageable_currentSortFallsBackToDefault() {
        Pageable pageable = PageRequest.of(0, 6, Sort.unsorted());
        Page<ArtistDto> artistPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        ModelMap modelMap = new ModelMap();

        when(artistService.findAll(pageable)).thenReturn(artistPage);

        artistController.artists(modelMap, pageable);

        assertEquals("id,desc", modelMap.get("currentSort"));
    }

    @Test
    void addArtist_validInputWithBandIds_redirectsToArtists() {
        ArtistDto artistDto = new ArtistDto();
        List<Long> bandIds = List.of(1L, 2L);
        MultipartFile artistImage = mock(MultipartFile.class);

        String view = artistController.addArtist(artistDto, bandIds, artistImage);

        assertEquals("redirect:/artists", view);
        verify(artistService).save(artistDto, artistImage, bandIds);
    }

    @Test
    void addArtist_withNullBandIds_stillCallsServiceAndRedirects() {
        ArtistDto artistDto = new ArtistDto();
        MultipartFile artistImage = mock(MultipartFile.class);

        String view = artistController.addArtist(artistDto, null, artistImage);

        assertEquals("redirect:/artists", view);
        verify(artistService).save(eq(artistDto), eq(artistImage), eq(null));
    }

    @Test
    void addArtist_serviceCalledWithCorrectArguments() {
        ArtistDto artistDto = new ArtistDto();
        List<Long> bandIds = List.of(10L);
        MultipartFile artistImage = mock(MultipartFile.class);

        artistController.addArtist(artistDto, bandIds, artistImage);

        verify(artistService).save(eq(artistDto), eq(artistImage), eq(bandIds));
    }

    @Test
    void addArtist_getRequest_returnsAddArtistViewWithBandsAndEmptyArtist() {
        List<BandDto> bands = List.of(new BandDto(), new BandDto());
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll()).thenReturn(bands);

        String view = artistController.addArtist(modelMap);

        assertEquals("addArtist", view);
        assertEquals(bands, modelMap.get("bands"));
        assertNotNull(modelMap.get("artist"));
        assertInstanceOf(Artist.class, modelMap.get("artist"));
        verify(bandService).findAll();
    }

    @Test
    void addArtist_getRequest_withNoBands_modelContainsEmptyList() {
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll()).thenReturn(Collections.emptyList());

        artistController.addArtist(modelMap);

        assertEquals(Collections.emptyList(), modelMap.get("bands"));
    }

    @Test
    void editArtist_validId_returnsEditArtistViewWithArtistAndBands() {
        Long id = 1L;
        ArtistDto artistDto = new ArtistDto();
        List<BandDto> bands = List.of(new BandDto());
        ModelMap modelMap = new ModelMap();

        when(artistService.getArtistById(id)).thenReturn(artistDto);
        when(bandService.findAll()).thenReturn(bands);

        String view = artistController.editArtist(id, modelMap);

        assertEquals("editArtist", view);
        assertEquals(artistDto, modelMap.get("artist"));
        assertEquals(bands, modelMap.get("bands"));
        verify(artistService).getArtistById(id);
        verify(bandService).findAll();
    }

    @Test
    void editArtist_serviceReturnsNull_modelContainsNullArtist() {
        Long id = 99L;
        ModelMap modelMap = new ModelMap();

        when(artistService.getArtistById(id)).thenReturn(null);
        when(bandService.findAll()).thenReturn(Collections.emptyList());

        String view = artistController.editArtist(id, modelMap);

        assertEquals("editArtist", view);
        assertEquals(null, modelMap.get("artist"));
    }

    @Test
    void editArtist_validInputWithBandIds_redirectsToArtists() {
        Long id = 1L;
        ArtistDto artistDto = new ArtistDto();
        List<Long> bandIds = List.of(2L, 3L);
        MultipartFile artistImage = mock(MultipartFile.class);

        String view = artistController.editArtist(id, artistDto, bandIds, artistImage);

        assertEquals("redirect:/artists", view);
        verify(artistService).update(id, artistDto, artistImage, bandIds);
    }

    @Test
    void editArtist_withNullBandIds_stillCallsServiceAndRedirects() {
        Long id = 5L;
        ArtistDto artistDto = new ArtistDto();
        MultipartFile artistImage = mock(MultipartFile.class);

        String view = artistController.editArtist(id, artistDto, null, artistImage);

        assertEquals("redirect:/artists", view);
        verify(artistService).update(eq(5L), eq(artistDto), eq(artistImage), eq(null));
    }

    @Test
    void editArtist_serviceCalledWithCorrectArguments() {
        Long id = 42L;
        ArtistDto artistDto = new ArtistDto();
        List<Long> bandIds = List.of(7L);
        MultipartFile artistImage = mock(MultipartFile.class);

        artistController.editArtist(id, artistDto, bandIds, artistImage);

        verify(artistService).update(eq(42L), eq(artistDto), eq(artistImage), eq(bandIds));
    }

    @Test
    void deleteArtist_validId_redirectsToArtists() {
        Long id = 1L;

        String view = artistController.deleteArtist(id);

        assertEquals("redirect:/artists", view);
        verify(artistService).delete(id);
    }

    @Test
    void deleteArtist_serviceCalledWithCorrectId() {
        Long id = 13L;

        artistController.deleteArtist(id);

        verify(artistService).delete(eq(13L));
    }

    @Test
    void artistPreviewPage_validId_returnsArtistPreviewViewWithModel() {
        Long id = 1L;
        ArtistDto artistDto = new ArtistDto();
        List<SongDto> songs = List.of(new SongDto(), new SongDto());
        ModelMap modelMap = new ModelMap();

        when(artistService.getArtistById(id)).thenReturn(artistDto);
        when(songService.getTop5SongsOfArtistByPlayCount(id)).thenReturn(songs);

        String view = artistController.artistPreviewPage(id, modelMap);

        assertEquals("artistPreview", view);
        assertEquals(artistDto, modelMap.get("artist"));
        assertEquals(songs, modelMap.get("songs"));
        verify(artistService).getArtistById(id);
        verify(songService).getTop5SongsOfArtistByPlayCount(id);
    }

    @Test
    void artistPreviewPage_emptyTopSongs_modelContainsEmptyList() {
        Long id = 2L;
        ArtistDto artistDto = new ArtistDto();
        ModelMap modelMap = new ModelMap();

        when(artistService.getArtistById(id)).thenReturn(artistDto);
        when(songService.getTop5SongsOfArtistByPlayCount(id)).thenReturn(Collections.emptyList());

        String view = artistController.artistPreviewPage(id, modelMap);

        assertEquals("artistPreview", view);
        @SuppressWarnings("unchecked")
        List<SongDto> songs = (List<SongDto>) modelMap.get("songs");
        assertEquals(Collections.emptyList(), songs);
    }

    @Test
    void artistPreviewPage_bothServicesCalledWithSameId() {
        Long id = 9L;
        ModelMap modelMap = new ModelMap();

        when(artistService.getArtistById(id)).thenReturn(new ArtistDto());
        when(songService.getTop5SongsOfArtistByPlayCount(id)).thenReturn(Collections.emptyList());

        artistController.artistPreviewPage(id, modelMap);

        verify(artistService).getArtistById(eq(9L));
        verify(songService).getTop5SongsOfArtistByPlayCount(eq(9L));
    }

    @Test
    void artistPreviewPage_artistIsNull_modelContainsNullArtist() {
        Long id = 404L;
        ModelMap modelMap = new ModelMap();

        when(artistService.getArtistById(id)).thenReturn(null);
        when(songService.getTop5SongsOfArtistByPlayCount(id)).thenReturn(Collections.emptyList());

        String view = artistController.artistPreviewPage(id, modelMap);

        assertEquals("artistPreview", view);
        assertEquals(null, modelMap.get("artist"));
    }
}