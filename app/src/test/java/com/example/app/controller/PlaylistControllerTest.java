package com.example.app.controller;

import com.example.dto.PlaylistDto;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Mock private PlaylistService playlistService;
    @Mock private SongService songService;
    @Mock private UserService userService;

    @InjectMocks private PlaylistController playlistController;

    @Test
    void playlists_happyPath_addsPlaylistsAndSortToModel() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<PlaylistDto> playlists = List.of(new PlaylistDto(), new PlaylistDto());
        ModelMap modelMap = new ModelMap();

        when(playlistService.findAll(sort)).thenReturn(playlists);
        when(playlistService.resolveCurrentSort(sort)).thenReturn("name,asc");

        String view = playlistController.playlists(modelMap, sort);

        assertEquals("playlists", view);
        assertEquals(playlists, modelMap.get("playlists"));
        assertEquals("name,asc", modelMap.get("currentSort"));
        verify(playlistService).findAll(sort);
        verify(playlistService).resolveCurrentSort(sort);
    }

    @Test
    void playlists_emptyList_addsEmptyListToModel() {
        Sort sort = Sort.unsorted();
        ModelMap modelMap = new ModelMap();

        when(playlistService.findAll(sort)).thenReturn(List.of());
        when(playlistService.resolveCurrentSort(sort)).thenReturn("");

        String view = playlistController.playlists(modelMap, sort);

        assertEquals("playlists", view);
        assertEquals(List.of(), modelMap.get("playlists"));
    }

    @Test
    void addPlaylist_authenticatedUser_createsPlaylistAndRedirects() {
        PlaylistDto playlistDto = new PlaylistDto();
        MultipartFile file = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);
        List<Long> songIds = List.of(1L, 2L);
        User user = new User();

        when(userDetails.getUsername()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));

        String view = playlistController.addPlaylist(playlistDto, file, userDetails, songIds);

        assertEquals("redirect:/playlists", view);
        verify(userService).findByUsername("alice");
        verify(playlistService).create(playlistDto, file, songIds, user);
    }

    @Test
    void addPlaylist_withNullSongIds_createsPlaylistWithNullSongIds() {
        PlaylistDto playlistDto = new PlaylistDto();
        MultipartFile file = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);
        User user = new User();

        when(userDetails.getUsername()).thenReturn("bob");
        when(userService.findByUsername("bob")).thenReturn(Optional.of(user));

        String view = playlistController.addPlaylist(playlistDto, file, userDetails, null);

        assertEquals("redirect:/playlists", view);
        verify(playlistService).create(playlistDto, file, null, user);
    }

    @Test
    void addPlaylist_userNotFound_throwsResponseStatusException() {
        PlaylistDto playlistDto = new PlaylistDto();
        MultipartFile file = mock(MultipartFile.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn("unknown");
        when(userService.findByUsername("unknown")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> playlistController.addPlaylist(playlistDto, file, userDetails, List.of()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(playlistService, never()).create(any(), any(), any(), any());
    }

    @Test
    void addPlaylist_getRequest_returnsAddPlaylistViewWithEmptyDto() {
        ModelMap modelMap = new ModelMap();

        String view = playlistController.addPlaylist(modelMap);

        assertEquals("addPlaylist", view);
        assertNotNull(modelMap.get("playlist"));
    }

    @Test
    void addPlaylist_getRequest_modelContainsFreshPlaylistDto() {
        ModelMap modelMap = new ModelMap();

        playlistController.addPlaylist(modelMap);

        Object playlist = modelMap.get("playlist");
        assertEquals(PlaylistDto.class, playlist.getClass());
    }

    @Test
    void editPlaylist_getRequest_addsPlaylistDtoToModel() {
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
    void editPlaylist_getRequest_serviceCalledWithCorrectId() {
        Long id = 42L;
        ModelMap modelMap = new ModelMap();
        when(playlistService.getPlaylistById(42L)).thenReturn(new PlaylistDto());

        playlistController.editPlaylist(id, modelMap);

        verify(playlistService).getPlaylistById(eq(42L));
    }

    @Test
    void editPlaylist_postRequest_updatesAndRedirects() {
        Long id = 1L;
        PlaylistDto playlistDto = new PlaylistDto();
        MultipartFile file = mock(MultipartFile.class);

        String view = playlistController.editPlaylist(id, playlistDto, file);

        assertEquals("redirect:/playlists", view);
        verify(playlistService).update(id, playlistDto, file);
    }

    @Test
    void editPlaylist_postRequest_serviceCalledWithCorrectArguments() {
        Long id = 7L;
        PlaylistDto playlistDto = new PlaylistDto();
        MultipartFile file = mock(MultipartFile.class);

        playlistController.editPlaylist(id, playlistDto, file);

        verify(playlistService).update(eq(7L), eq(playlistDto), eq(file));
    }

    @Test
    void deletePlaylist_validId_deletesAndRedirects() {
        Long id = 1L;

        String view = playlistController.deletePlaylist(id);

        assertEquals("redirect:/playlists", view);
        verify(playlistService).delete(id);
    }

    @Test
    void deletePlaylist_serviceCalledWithCorrectId() {
        playlistController.deletePlaylist(99L);

        verify(playlistService).delete(eq(99L));
    }

    @Test
    void playlistPreviewPage_validId_addsPlaylistToModel() {
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
    void playlistPreviewPage_serviceCalledWithCorrectId() {
        Long id = 5L;
        ModelMap modelMap = new ModelMap();
        when(playlistService.getPlaylistById(5L)).thenReturn(new PlaylistDto());

        playlistController.playlistPreviewPage(id, modelMap);

        verify(playlistService).getPlaylistById(eq(5L));
    }

    @Test
    void setVisibility_public_setsVisibilityAndRedirectsToPreview() {
        Long id = 3L;

        String view = playlistController.setVisibility(id, true);

        assertEquals("redirect:/playlists/preview?id=3", view);
        verify(playlistService).setVisibility(3L, true);
    }

    @Test
    void setVisibility_private_setsVisibilityAndRedirectsToPreview() {
        Long id = 4L;

        String view = playlistController.setVisibility(id, false);

        assertEquals("redirect:/playlists/preview?id=4", view);
        verify(playlistService).setVisibility(4L, false);
    }

    @Test
    void setVisibility_serviceCalledWithCorrectArguments() {
        playlistController.setVisibility(10L, true);

        verify(playlistService).setVisibility(eq(10L), eq(true));
    }

    @Test
    void addSongToPlaylist_validIds_addsSongAndRedirectsToPreview() {
        Long playlistId = 1L;
        Long songId = 2L;

        String view = playlistController.addSongToPlaylist(playlistId, songId);

        assertEquals("redirect:/playlists/preview?id=1", view);
        verify(playlistService).addSong(playlistId, songId);
    }

    @Test
    void addSongToPlaylist_serviceCalledWithCorrectArguments() {
        playlistController.addSongToPlaylist(5L, 10L);

        verify(playlistService).addSong(eq(5L), eq(10L));
    }

    @Test
    void addSongToPlaylist_redirectContainsPlaylistId() {
        Long playlistId = 99L;

        String view = playlistController.addSongToPlaylist(playlistId, 1L);

        assertEquals("redirect:/playlists/preview?id=99", view);
    }

    @Test
    void removeSongFromPlaylist_validIds_removesSongAndRedirectsToPreview() {
        Long playlistId = 1L;
        Long songId = 2L;

        String view = playlistController.removeSongFromPlaylist(playlistId, songId);

        assertEquals("redirect:/playlists/preview?id=1", view);
        verify(playlistService).removeSong(playlistId, songId);
    }

    @Test
    void removeSongFromPlaylist_serviceCalledWithCorrectArguments() {
        playlistController.removeSongFromPlaylist(7L, 3L);

        verify(playlistService).removeSong(eq(7L), eq(3L));
    }

    @Test
    void removeSongFromPlaylist_redirectContainsPlaylistId() {
        Long playlistId = 55L;

        String view = playlistController.removeSongFromPlaylist(playlistId, 1L);

        assertEquals("redirect:/playlists/preview?id=55", view);
    }
}