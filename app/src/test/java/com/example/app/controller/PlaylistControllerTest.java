package com.example.app.controller;

import com.example.dto.PlaylistDto;
import com.example.model.Playlist;
import com.example.model.User;
import com.example.service.PlaylistService;
import com.example.service.SongService;
import com.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    @Mock
    private PlaylistService playlistService;

    @Mock
    private SongService songService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PlaylistController playlistController;

    @Test
    void playlists_withSort_returnsPlaylistsViewWithModelAttributes() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<Playlist> playlistList = List.of(new Playlist(), new Playlist());
        ModelMap modelMap = new ModelMap();

        when(playlistService.findAll(sort)).thenReturn(playlistList);
        when(playlistService.resolveCurrentSort(sort)).thenReturn("name,asc");
        
        String view = playlistController.playlists(modelMap, sort);

        assertEquals("playlists", view);
        assertEquals(playlistList, modelMap.get("playlists"));
        assertEquals("name,asc", modelMap.get("currentSort"));
        verify(playlistService).findAll(sort);
        verify(playlistService).resolveCurrentSort(sort);
    }

    @Test
    void playlists_withEmptyList_modelContainsEmptyPlaylists() {
        Sort sort = Sort.unsorted();
        ModelMap modelMap = new ModelMap();

        when(playlistService.findAll(sort)).thenReturn(Collections.emptyList());
        when(playlistService.resolveCurrentSort(sort)).thenReturn("id,desc");

        String view = playlistController.playlists(modelMap, sort);

        assertEquals("playlists", view);
        assertEquals(Collections.emptyList(), modelMap.get("playlists"));
    }

    @Test
    void playlists_unsortedSort_currentSortFallbackPopulated() {
        Sort sort = Sort.unsorted();
        ModelMap modelMap = new ModelMap();

        when(playlistService.findAll(sort)).thenReturn(Collections.emptyList());
        when(playlistService.resolveCurrentSort(sort)).thenReturn("id,desc");

        playlistController.playlists(modelMap, sort);

        assertEquals("id,desc", modelMap.get("currentSort"));
    }

    @Test
    void addPlaylist_validUser_setsUserAndCreatesPlaylistAndRedirects() {
        Playlist playlist = new Playlist();
        MultipartFile multipartFile = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);
        List<Long> songIds = List.of(1L, 2L);
        User user = new User();

        when(userDetails.getUsername()).thenReturn("john");
        when(userService.findByUsername("john")).thenReturn(Optional.of(user));
        
        String view = playlistController.addPlaylist(playlist, multipartFile, userDetails, songIds);
        
        assertEquals("redirect:/playlists", view);
        assertEquals(user, playlist.getUser());
        verify(userService).findByUsername("john");
        verify(playlistService).create(playlist, multipartFile, songIds);
    }

    @Test
    void addPlaylist_withNullSongIds_stillCreatesPlaylistAndRedirects() {
        Playlist playlist = new Playlist();
        MultipartFile multipartFile = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);
        User user = new User();

        when(userDetails.getUsername()).thenReturn("jane");
        when(userService.findByUsername("jane")).thenReturn(Optional.of(user));
        
        String view = playlistController.addPlaylist(playlist, multipartFile, userDetails, null);

        assertEquals("redirect:/playlists", view);
        verify(playlistService).create(eq(playlist), eq(multipartFile), eq(null));
    }

    @Test
    void addPlaylist_userNotFound_throwsResponseStatusExceptionUnauthorized() {
        Playlist playlist = new Playlist();
        MultipartFile multipartFile = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn("ghost");
        when(userService.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> playlistController.addPlaylist(playlist, multipartFile, userDetails, null));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(playlistService, never()).create(any(), any(), any());
    }

    @Test
    void addPlaylist_playlistUserIsSetBeforeCreate() {
        Playlist playlist = new Playlist();
        MultipartFile multipartFile = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);
        User user = new User();

        when(userDetails.getUsername()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        
        playlistController.addPlaylist(playlist, multipartFile, userDetails, List.of());

        assertNotNull(playlist.getUser());
        assertEquals(user, playlist.getUser());
    }

    @Test
    void addPlaylist_getRequest_returnsAddPlaylistViewWithEmptyPlaylist() {
        ModelMap modelMap = new ModelMap();

        String view = playlistController.addPlaylist(modelMap);
        
        assertEquals("addPlaylist", view);
        assertNotNull(modelMap.get("playlist"));
        assertInstanceOf(Playlist.class, modelMap.get("playlist"));
    }

    @Test
    void editPlaylist_validId_returnsEditPlaylistViewWithPlaylistDto() {
        Long id = 1L;
        PlaylistDto playlistDto = new PlaylistDto();
        ModelMap modelMap = new ModelMap();

        when(playlistService.getPlaylistById(id)).thenReturn(playlistDto);

        String view = playlistController.editPlaylist(id, modelMap);

        assertEquals("editPlaylist", view);
        assertEquals(playlistDto, modelMap.get("playlist"));
        verify(playlistService).getPlaylistById(id);
    }

    @Test
    void editPlaylist_serviceReturnsNull_modelContainsNull() {
        Long id = 99L;
        ModelMap modelMap = new ModelMap();

        when(playlistService.getPlaylistById(id)).thenReturn(null);
        
        String view = playlistController.editPlaylist(id, modelMap);
        
        assertEquals("editPlaylist", view);
        assertEquals(null, modelMap.get("playlist"));
    }

    @Test
    void editPlaylist_validInput_updatesAndRedirectsToPlaylists() {
        Playlist editedPlaylist = new Playlist();
        MultipartFile multipartFile = mock(MultipartFile.class);

        String view = playlistController.editPlaylist(editedPlaylist, multipartFile);
        
        assertEquals("redirect:/playlists", view);
        verify(playlistService).update(editedPlaylist, multipartFile);
    }

    @Test
    void editPlaylist_serviceCalledWithCorrectArguments() {
        Playlist editedPlaylist = new Playlist();
        MultipartFile multipartFile = mock(MultipartFile.class);
        
        playlistController.editPlaylist(editedPlaylist, multipartFile);

        verify(playlistService).update(eq(editedPlaylist), eq(multipartFile));
    }

    @Test
    void deletePlaylist_validId_deletesAndRedirectsToPlaylists() {
        Long id = 1L;

        String view = playlistController.deletePlaylist(id);
        
        assertEquals("redirect:/playlists", view);
        verify(playlistService).delete(id);
    }

    @Test
    void deletePlaylist_serviceCalledWithCorrectId() {
        Long id = 55L;

        playlistController.deletePlaylist(id);

        verify(playlistService).delete(eq(55L));
    }

    @Test
    void playlistPreviewPage_validId_returnsPlaylistPreviewViewWithDto() {
        Long id = 1L;
        PlaylistDto playlistDto = new PlaylistDto();
        ModelMap modelMap = new ModelMap();

        when(playlistService.getPlaylistById(id)).thenReturn(playlistDto);

        String view = playlistController.playlistPreviewPage(id, modelMap);

        assertEquals("playlistPreview", view);
        assertEquals(playlistDto, modelMap.get("playlist"));
        verify(playlistService).getPlaylistById(id);
    }

    @Test
    void playlistPreviewPage_serviceReturnsNull_modelContainsNull() {
        Long id = 404L;
        ModelMap modelMap = new ModelMap();

        when(playlistService.getPlaylistById(id)).thenReturn(null);
        
        String view = playlistController.playlistPreviewPage(id, modelMap);

        assertEquals("playlistPreview", view);
        assertEquals(null, modelMap.get("playlist"));
    }

    @Test
    void setVisibility_publicTrue_setsVisibilityAndRedirectsToPreview() {
        Long id = 1L;
        boolean isPublic = true;
        
        String view = playlistController.setVisibility(id, isPublic);
        
        assertEquals("redirect:/playlists/preview?id=" + id, view);
        verify(playlistService).setVisibility(id, isPublic);
    }

    @Test
    void setVisibility_publicFalse_setsVisibilityAndRedirectsToPreview() {
        Long id = 2L;
        boolean isPublic = false;
        
        String view = playlistController.setVisibility(id, isPublic);

        assertEquals("redirect:/playlists/preview?id=2", view);
        verify(playlistService).setVisibility(eq(2L), eq(false));
    }

    @Test
    void setVisibility_redirectUrlContainsCorrectId() {
        Long id = 99L;

        String view = playlistController.setVisibility(id, true);
        
        assertEquals("redirect:/playlists/preview?id=99", view);
    }

    @Test
    void addSongToPlaylist_validIds_addsSongAndRedirectsToPreview() {
        Long playlistId = 1L;
        Long songId = 10L;
        
        String view = playlistController.addSongToPlaylist(playlistId, songId);
        
        assertEquals("redirect:/playlists/preview?id=" + playlistId, view);
        verify(playlistService).addSong(playlistId, songId);
    }

    @Test
    void addSongToPlaylist_serviceCalledWithCorrectArguments() {
        Long playlistId = 5L;
        Long songId = 20L;
        
        playlistController.addSongToPlaylist(playlistId, songId);
        
        verify(playlistService).addSong(eq(5L), eq(20L));
    }

    @Test
    void addSongToPlaylist_redirectUrlContainsCorrectPlaylistId() {
        Long playlistId = 42L;
        Long songId = 7L;
        
        String view = playlistController.addSongToPlaylist(playlistId, songId);
        
        assertEquals("redirect:/playlists/preview?id=42", view);
    }

    @Test
    void removeSongFromPlaylist_validIds_removesSongAndRedirectsToPreview() {
        Long playlistId = 1L;
        Long songId = 10L;
        
        String view = playlistController.removeSongFromPlaylist(playlistId, songId);
        
        assertEquals("redirect:/playlists/preview?id=" + playlistId, view);
        verify(playlistService).removeSong(playlistId, songId);
    }

    @Test
    void removeSongFromPlaylist_serviceCalledWithCorrectArguments() {
        Long playlistId = 3L;
        Long songId = 15L;

        playlistController.removeSongFromPlaylist(playlistId, songId);

        verify(playlistService).removeSong(eq(3L), eq(15L));
    }

    @Test
    void removeSongFromPlaylist_redirectUrlContainsCorrectPlaylistId() {
        Long playlistId = 77L;
        Long songId = 9L;
        
        String view = playlistController.removeSongFromPlaylist(playlistId, songId);

        assertEquals("redirect:/playlists/preview?id=77", view);
    }
}