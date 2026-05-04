package com.example.app.controller;

import com.example.dto.SongDto;
import com.example.model.Genre;
import com.example.model.Song;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    @Mock
    private SongService songService;

    @Mock
    private ArtistService artistService;

    @Mock
    private BandService bandService;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private SongController songController;

    @Test
    void songs_withGenreAndPageable_returnsSongsViewWithAllModelAttributes() {
        Genre genre = Genre.ROCK;
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<SongDto> songPage = new PageImpl<>(List.of(new SongDto()));
        List<Integer> pageNumbers = List.of(1);
        ModelMap modelMap = new ModelMap();

        when(songService.findSongsByGenre(genre, pageable)).thenReturn(songPage);
        when(songService.getPageNumbers(songPage)).thenReturn(pageNumbers);
        
        String view = songController.songs(modelMap, genre, pageable);

        assertEquals("songs", view);
        assertEquals(songPage, modelMap.get("songs"));
        assertEquals(genre, modelMap.get("selectedGenre"));
        assertEquals(pageNumbers, modelMap.get("pageNumbers"));
        assertNotNull(modelMap.get("genres"));
        assertEquals("id,desc", modelMap.get("currentSort"));
        verify(songService).findSongsByGenre(genre, pageable);
        verify(songService).getPageNumbers(songPage);
    }

    @Test
    void songs_withNullGenre_passesNullToService() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<SongDto> songPage = new PageImpl<>(Collections.emptyList());
        ModelMap modelMap = new ModelMap();

        when(songService.findSongsByGenre(null, pageable)).thenReturn(songPage);
        when(songService.getPageNumbers(songPage)).thenReturn(Collections.emptyList());
        
        String view = songController.songs(modelMap, null, pageable);
        
        assertEquals("songs", view);
        assertEquals(null, modelMap.get("selectedGenre"));
        verify(songService).findSongsByGenre(null, pageable);
    }

    @Test
    void songs_genresAttributeContainsAllGenreValues() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name"));
        Page<SongDto> songPage = new PageImpl<>(Collections.emptyList());
        ModelMap modelMap = new ModelMap();

        when(songService.findSongsByGenre(null, pageable)).thenReturn(songPage);
        when(songService.getPageNumbers(songPage)).thenReturn(Collections.emptyList());

        songController.songs(modelMap, null, pageable);
        
        Genre[] genres = (Genre[]) modelMap.get("genres");
        assertNotNull(genres);
        assertEquals(Arrays.asList(Genre.values()), Arrays.asList(genres));
    }

    @Test
    void songs_withSortByName_currentSortPopulatedCorrectly() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name"));
        Page<SongDto> songPage = new PageImpl<>(Collections.emptyList());
        ModelMap modelMap = new ModelMap();

        when(songService.findSongsByGenre(null, pageable)).thenReturn(songPage);
        when(songService.getPageNumbers(songPage)).thenReturn(Collections.emptyList());
        
        songController.songs(modelMap, null, pageable);

        assertEquals("name,asc", modelMap.get("currentSort"));
    }

    @Test
    void songs_withUnsortedPageable_currentSortFallsBackToDefault() {
        Pageable pageable = PageRequest.of(0, 5, Sort.unsorted());
        Page<SongDto> songPage = new PageImpl<>(Collections.emptyList());
        ModelMap modelMap = new ModelMap();

        when(songService.findSongsByGenre(null, pageable)).thenReturn(songPage);
        when(songService.getPageNumbers(songPage)).thenReturn(Collections.emptyList());
        
        songController.songs(modelMap, null, pageable);
        
        assertEquals("id,desc", modelMap.get("currentSort"));
    }

    @Test
    void addSong_validInput_savesSongAndRedirectsWithFlashAttribute() {
        Song song = new Song();
        MultipartFile multipartFile = mock(MultipartFile.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        
        String view = songController.addSong(song, multipartFile, redirectAttributes);

        assertEquals("redirect:/songs", view);
        verify(songService).save(song, multipartFile);
        verify(redirectAttributes).addFlashAttribute("success", "Song added successfully!");
    }

    @Test
    void addSong_serviceCalledWithCorrectArguments() {
        Song song = new Song();
        MultipartFile multipartFile = mock(MultipartFile.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        
        songController.addSong(song, multipartFile, redirectAttributes);

        verify(songService).save(eq(song), eq(multipartFile));
    }

    @Test
    void addSong_getRequest_returnsAddSongViewWithAllModelAttributes() {
        ModelMap modelMap = new ModelMap();
        List<Object> artists = List.of(new Object());
        List<Object> bands = List.of(new Object());
        List<Object> albums = List.of(new Object());

        doReturn(artists).when(artistService).findAll();
        doReturn(bands).when(bandService).findAll();
        doReturn(albums).when(albumService).findAll();

        String view = songController.addSong(modelMap);

        assertEquals("addSong", view);
        assertNotNull(modelMap.get("genres"));
        assertEquals(artists, modelMap.get("artists"));
        assertEquals(bands, modelMap.get("bands"));
        assertEquals(albums, modelMap.get("albums"));
        verify(artistService).findAll();
        verify(bandService).findAll();
        verify(albumService).findAll();
    }

    @Test
    void addSong_getRequest_genresContainsAllValues() {
        ModelMap modelMap = new ModelMap();

        doReturn(Collections.emptyList()).when(artistService).findAll();
        doReturn(Collections.emptyList()).when(bandService).findAll();
        doReturn(Collections.emptyList()).when(albumService).findAll();

        songController.addSong(modelMap);
        
        Genre[] genres = (Genre[]) modelMap.get("genres");
        assertEquals(Arrays.asList(Genre.values()), Arrays.asList(genres));
    }

    @Test
    void deleteSong_validId_deletesAndRedirectsToSongs() {
        Long id = 1L;

        String view = songController.deleteSong(id);

        assertEquals("redirect:/songs", view);
        verify(songService).delete(id);
    }

    @Test
    void deleteSong_serviceCalledWithCorrectId() {
        Long id = 42L;
        
        songController.deleteSong(id);
        
        verify(songService).delete(eq(42L));
    }

    @Test
    void registerPlay_validId_registersPlayAndRedirectsToSongs() {
        Long id = 1L;

        String view = songController.registerPlay(id);
        
        assertEquals("redirect:/songs", view);
        verify(songService).registerPlay(id);
    }

    @Test
    void registerPlay_serviceCalledWithCorrectId() {
        Long id = 7L;

        songController.registerPlay(id);
        
        verify(songService).registerPlay(eq(7L));
    }

    @Test
    void search_withQueryAndLimit_returnsSongDtoList() {
        String q = "rock";
        int limit = 5;
        List<SongDto> results = List.of(new SongDto(), new SongDto());

        when(songService.searchSongs(q, limit)).thenReturn(results);

        List<SongDto> actual = songController.search(q, limit);
        
        assertEquals(results, actual);
        verify(songService).searchSongs(q, limit);
    }

    @Test
    void search_withEmptyQuery_returnsResults() {
        String q = "";
        int limit = 10;
        List<SongDto> results = List.of(new SongDto());
        when(songService.searchSongs(q, limit)).thenReturn(results);

        List<SongDto> actual = songController.search(q, limit);

        assertEquals(results, actual);
        verify(songService).searchSongs(eq(""), eq(10));
    }

    @Test
    void search_serviceReturnsEmptyList_returnsEmptyList() {
        when(songService.searchSongs(any(), any(Integer.class))).thenReturn(Collections.emptyList());
        
        List<SongDto> actual = songController.search("nothing", 3);
        
        assertEquals(Collections.emptyList(), actual);
    }

    @Test
    void search_serviceCalledWithCorrectArguments() {
        when(songService.searchSongs("jazz", 20)).thenReturn(Collections.emptyList());

        songController.search("jazz", 20);

        verify(songService).searchSongs(eq("jazz"), eq(20));
    }

    @Test
    void initBinder_setsDisallowedFields() {
        WebDataBinder binder = mock(WebDataBinder.class);
        
        songController.initBinder(binder);

        verify(binder).setDisallowedFields("performer_type", "has_album", "songUrl");
    }
}