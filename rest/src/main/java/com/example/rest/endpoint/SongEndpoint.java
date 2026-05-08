package com.example.rest.endpoint;

import com.example.dto.SongCommentDto;
import com.example.dto.SongCommentReactionRequest;
import com.example.dto.SongCommentRequest;
import com.example.dto.SongDto;
import com.example.model.Genre;
import com.example.rest.service.security.SpringUser;
import com.example.service.SongCommentReactionService;
import com.example.service.SongCommentService;
import com.example.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongEndpoint {

    private final SongService songService;
    private final SongCommentService songCommentService;
    private final SongCommentReactionService songCommentReactionService;

    @GetMapping
    public ResponseEntity<Page<SongDto>> getSongs(
            @RequestParam(required = false) Genre genre,
            @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(songService.findSongsByGenre(genre, pageable));
    }

    @GetMapping("/{id}")
    public SongDto getSong(@PathVariable Long id) {
        return songService.getSongDtoById(id);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<SongPreviewResponse> getSongPreview(@PathVariable Long id) {
        SongDto song = songService.getSongById(id);
        List<SongDto> albumSongs = songService.getSongsByAlbumId(song.getAlbumId());
        return ResponseEntity.ok(new SongPreviewResponse(song, albumSongs));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongDto>> searchSongs(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(songService.searchSongs(q, limit));
    }

    @GetMapping("/top")
    public ResponseEntity<List<SongDto>> getTopSongs(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(songService.findTopByPlayCount(limit));
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<SongDto>> getSongsByAlbum(@PathVariable Long albumId) {
        return ResponseEntity.ok(songService.getSongsByAlbumId(albumId));
    }


    @GetMapping("/{albumId}/comments")
    public Page<SongCommentDto> viewComments(@PageableDefault Pageable pageable, @PathVariable Long albumId) {
        return songCommentService.findAllDto(pageable, albumId);
    }

    @GetMapping("/comments/{id}")
    public SongCommentDto viewComment(@PathVariable Long id) {
        return songCommentService.getSongComment(id);
    }

    @PostMapping("/comments")
    public SongCommentDto saveComment(@RequestBody @Valid SongCommentRequest request, @AuthenticationPrincipal SpringUser user) {
        return songCommentService.createSongComment(request, user.getUser());
    }

    @PostMapping("/comments/rate")
    public SongCommentDto saveReaction(@RequestBody @Valid SongCommentReactionRequest request, @AuthenticationPrincipal SpringUser user) {
        songCommentReactionService.saveCommentReaction(request, user.getUser());
        return viewComment(request.getCommentId());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SongDto> createSong(
            @RequestPart("song") SongDto songDto,
            @RequestPart("songFile") MultipartFile songFile) {
        return ResponseEntity.status(201).body(songService.save(songDto, songFile));
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<Void> registerPlay(@PathVariable Long id) {
        songService.registerPlay(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record SongPreviewResponse(SongDto song, List<SongDto> albumSongs) {}
}