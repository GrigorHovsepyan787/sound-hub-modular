package com.example.app.controller;

import com.example.dto.PlaylistDto;
import com.example.model.User;
import com.example.service.PlaylistService;
import com.example.service.SongService;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final SongService songService;
    private final UserService userService;

    @GetMapping("/playlists")
    public String playlists(ModelMap modelMap, Sort sort) {

        List<PlaylistDto> playlists = playlistService.findAll(sort);

        modelMap.addAttribute("playlists", playlists);
        modelMap.addAttribute("currentSort", playlistService.resolveCurrentSort(sort));

        return "playlists";
    }

    @PostMapping("/playlists")
    public String addPlaylist(@ModelAttribute PlaylistDto playlistDto,
                              @RequestParam("playlistImage") MultipartFile multipartFile,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(value = "songIds", required = false) List<Long> songIds) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        playlistService.create(playlistDto, multipartFile, songIds, user);
        return "redirect:/playlists";
    }

    @GetMapping("/playlists/add")
    public String addPlaylist(ModelMap modelMap) {
        modelMap.addAttribute("playlist", new PlaylistDto());
        return "addPlaylist";
    }

    @GetMapping("/playlists/edit")
    public String editPlaylist(@RequestParam("id") Long id, ModelMap modelMap) {
        PlaylistDto playlistDto = playlistService.getPlaylistById(id);
        modelMap.addAttribute("playlist", playlistDto);
        return "editPlaylist";
    }

    @PostMapping("/playlists/edit")
    public String editPlaylist(@RequestParam("id") Long id,
                               @ModelAttribute PlaylistDto playlistDto,
                               @RequestParam("playlistImage") MultipartFile multipartFile) {
        playlistService.update(id, playlistDto, multipartFile);
        return "redirect:/playlists";
    }

    @PostMapping("/playlists/delete")
    public String deletePlaylist(@RequestParam("id") Long id) {
        playlistService.delete(id);
        return "redirect:/playlists";
    }

    @GetMapping("/playlists/preview")
    public String playlistPreviewPage(@RequestParam("id") Long id, ModelMap modelMap) {
        PlaylistDto playlistDto = playlistService.getPlaylistById(id);
        modelMap.addAttribute("playlist", playlistDto);
        return "playlistPreview";
    }

    @PostMapping("/playlists/set-visibility")
    public String setVisibility(@RequestParam Long id,
                                @RequestParam boolean isPublic) {
        playlistService.setVisibility(id, isPublic);
        return "redirect:/playlists/preview?id=" + id;
    }

    @PostMapping("/playlists/add-song")
    public String addSongToPlaylist(@RequestParam Long playlistId,
                                    @RequestParam Long songId) {
        playlistService.addSong(playlistId, songId);
        return "redirect:/playlists/preview?id=" + playlistId;
    }

    @PostMapping("/playlists/remove-song")
    public String removeSongFromPlaylist(@RequestParam Long playlistId,
                                         @RequestParam Long songId) {
        playlistService.removeSong(playlistId, songId);
        return "redirect:/playlists/preview?id=" + playlistId;
    }
}