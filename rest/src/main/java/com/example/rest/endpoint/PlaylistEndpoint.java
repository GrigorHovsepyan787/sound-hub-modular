package com.example.rest.endpoint;

import com.example.dto.PlaylistDto;
import com.example.model.User;
import com.example.service.PlaylistService;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistEndpoint {

    private final PlaylistService playlistService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<PlaylistDto>> getAllPlaylists(
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC) Sort sort) {
        return ResponseEntity.ok(playlistService.findAll(sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDto> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaylistDto> createPlaylist(
            @RequestPart("playlist") PlaylistDto playlistDto,
            @RequestPart(value = "playlistImage", required = false) MultipartFile multipartFile,
            @RequestParam(value = "songIds", required = false) List<Long> songIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        PlaylistDto created = playlistService.create(playlistDto, multipartFile, songIds, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaylistDto> updatePlaylist(
            @PathVariable Long id,
            @RequestPart("playlist") PlaylistDto playlistDto,
            @RequestPart(value = "playlistImage", required = false) MultipartFile multipartFile) {
        return ResponseEntity.ok(playlistService.update(id, playlistDto, multipartFile));
    }

    @DeleteMapping("/{id}")
    public void deletePlaylist(@PathVariable Long id) {
        playlistService.delete(id);
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Void> setVisibility(@PathVariable Long id,
                                              @RequestParam boolean isPublic) {
        playlistService.setVisibility(id, isPublic);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public void addSong(@PathVariable Long playlistId,
                        @PathVariable Long songId) {
        playlistService.addSong(playlistId, songId);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public void removeSong(@PathVariable Long playlistId,
                           @PathVariable Long songId) {
        playlistService.removeSong(playlistId, songId);
    }
}