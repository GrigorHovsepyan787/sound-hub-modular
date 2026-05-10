package com.example.app.controller;

import com.example.dto.BandDto;
import com.example.dto.SongDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BandControllerTest {

    @Mock
    private BandService bandService;

    @Mock
    private SongService songService;

    @InjectMocks
    private BandController bandController;

    @Test
    void bands_withMultiplePages_returnsCorrectViewAndPageNumbers() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"));
        List<BandDto> bandList = List.of(new BandDto(), new BandDto());
        Page<BandDto> bandPage = new PageImpl<>(bandList, pageable, 12);
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll(pageable)).thenReturn(bandPage);
        when(bandService.getPageNumbers(bandPage)).thenReturn(List.of(1, 2));

        String view = bandController.bands(modelMap, pageable);

        assertEquals("bands", view);
        assertEquals(bandPage, modelMap.get("bands"));

        @SuppressWarnings("unchecked")
        List<Integer> pageNumbers = (List<Integer>) modelMap.get("pageNumbers");

        assertEquals(List.of(1, 2), pageNumbers);

        verify(bandService).findAll(pageable);
        verify(bandService).getPageNumbers(bandPage);
    }

    @Test
    void bands_withSinglePage_returnsPageNumberListOfOne() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"));
        Page<BandDto> bandPage = new PageImpl<>(List.of(new BandDto()), pageable, 1);
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll(pageable)).thenReturn(bandPage);
        when(bandService.getPageNumbers(bandPage)).thenReturn(List.of(1));

        bandController.bands(modelMap, pageable);

        @SuppressWarnings("unchecked")
        List<Integer> pageNumbers = (List<Integer>) modelMap.get("pageNumbers");

        assertEquals(List.of(1), pageNumbers);

        verify(bandService).getPageNumbers(bandPage);
    }

    @Test
    void bands_withZeroTotalPages_returnsEmptyPageNumbers() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"));
        Page<BandDto> bandPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll(pageable)).thenReturn(bandPage);
        
        bandController.bands(modelMap, pageable);
        
        @SuppressWarnings("unchecked")
        List<Integer> pageNumbers = (List<Integer>) modelMap.get("pageNumbers");
        assertEquals(Collections.emptyList(), pageNumbers);
    }

    @Test
    void bands_currentSortAttribute_isPopulatedCorrectly() {
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "name"));
        Page<BandDto> bandPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll(pageable)).thenReturn(bandPage);
        
        bandController.bands(modelMap, pageable);
        
        assertEquals("name,asc", modelMap.get("currentSort"));
    }

    @Test
    void bands_withUnsortedPageable_currentSortFallsBackToDefault() {
        Pageable pageable = PageRequest.of(0, 6, Sort.unsorted());
        Page<BandDto> bandPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        ModelMap modelMap = new ModelMap();

        when(bandService.findAll(pageable)).thenReturn(bandPage);

        bandController.bands(modelMap, pageable);
        
        assertEquals("id,desc", modelMap.get("currentSort"));
    }

    @Test
    void addBand_validInput_redirectsToBands() {
        BandDto bandDto = new BandDto();
        MultipartFile bandImage = mock(MultipartFile.class);

        String view = bandController.addBand(bandDto, bandImage);
        
        assertEquals("redirect:/bands", view);
        verify(bandService).create(bandDto, bandImage);
    }

    @Test
    void addBand_serviceCalledWithCorrectArguments() {
        BandDto bandDto = new BandDto();
        MultipartFile bandImage = mock(MultipartFile.class);
        
        bandController.addBand(bandDto, bandImage);
        
        verify(bandService).create(eq(bandDto), eq(bandImage));
    }

    @Test
    void addBand_getRequest_returnsAddBandView() {
        String view = bandController.addBand();
        
        assertEquals("addBand", view);
    }

    @Test
    void editBand_validId_returnsEditBandViewWithBandInModel() {
        Long id = 1L;
        BandDto bandDto = new BandDto();
        ModelMap modelMap = new ModelMap();

        when(bandService.getBandById(id)).thenReturn(bandDto);
        
        String view = bandController.editBand(id, modelMap);
        
        assertEquals("editBand", view);
        assertEquals(bandDto, modelMap.get("band"));
        verify(bandService).getBandById(id);
    }

    @Test
    void editBand_serviceReturnsNull_modelContainsNull() {
        Long id = 99L;
        ModelMap modelMap = new ModelMap();

        when(bandService.getBandById(id)).thenReturn(null);
        
        String view = bandController.editBand(id, modelMap);
        
        assertEquals("editBand", view);
        assertEquals(null, modelMap.get("band"));
    }

    @Test
    void editBand_validInput_redirectsToBands() {
        Long id = 1L;
        BandDto bandDto = new BandDto();
        MultipartFile bandImage = mock(MultipartFile.class);

        String view = bandController.editBand(id, bandDto, bandImage);
        
        assertEquals("redirect:/bands", view);
        verify(bandService).update(id, bandDto, bandImage);
    }

    @Test
    void editBand_serviceCalledWithCorrectArguments() {
        
        Long id = 42L;
        BandDto bandDto = new BandDto();
        MultipartFile bandImage = mock(MultipartFile.class);
        
        bandController.editBand(id, bandDto, bandImage);

        verify(bandService).update(eq(42L), eq(bandDto), eq(bandImage));
    }

    @Test
    void deleteBand_validId_redirectsToBands() {
        Long id = 1L;

        String view = bandController.deleteBand(id);
        
        assertEquals("redirect:/bands", view);
        verify(bandService).delete(id);
    }

    @Test
    void deleteBand_serviceCalledWithCorrectId() {
        Long id = 7L;

        bandController.deleteBand(id);

        verify(bandService).delete(eq(7L));
    }

    @Test
    void bandPreviewPage_validId_returnsBandPreviewViewWithModel() {
        
        Long id = 1L;
        BandDto bandDto = new BandDto();
        List<SongDto> songs = List.of(new SongDto(), new SongDto());
        ModelMap modelMap = new ModelMap();

        when(bandService.getBandByIdForArtists(id)).thenReturn(bandDto);
        when(songService.getTop5SongsOfBandByPlayCount(id)).thenReturn(songs);
        
        String view = bandController.bandPreviewPage(id, modelMap);

        assertEquals("bandPreview", view);
        assertEquals(bandDto, modelMap.get("band"));
        assertEquals(songs, modelMap.get("songs"));
        verify(bandService).getBandByIdForArtists(id);
        verify(songService).getTop5SongsOfBandByPlayCount(id);
    }

    @Test
    void bandPreviewPage_emptyTopSongs_modelContainsEmptyList() {
        Long id = 2L;
        BandDto bandDto = new BandDto();
        ModelMap modelMap = new ModelMap();

        when(bandService.getBandByIdForArtists(id)).thenReturn(bandDto);
        when(songService.getTop5SongsOfBandByPlayCount(id)).thenReturn(Collections.emptyList());
        
        String view = bandController.bandPreviewPage(id, modelMap);
        
        assertEquals("bandPreview", view);
        @SuppressWarnings("unchecked")
        List<SongDto> songs = (List<SongDto>) modelMap.get("songs");
        assertEquals(Collections.emptyList(), songs);
    }

    @Test
    void bandPreviewPage_bothServicesCalledWithSameId() {
        Long id = 5L;
        ModelMap modelMap = new ModelMap();

        when(bandService.getBandByIdForArtists(id)).thenReturn(new BandDto());
        when(songService.getTop5SongsOfBandByPlayCount(id)).thenReturn(Collections.emptyList());

        bandController.bandPreviewPage(id, modelMap);

        verify(bandService).getBandByIdForArtists(eq(5L));
        verify(songService).getTop5SongsOfBandByPlayCount(eq(5L));
    }
}