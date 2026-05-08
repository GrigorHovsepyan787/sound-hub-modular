package com.example.rest.endpoint;

import com.example.dto.AlbumCommentDto;
import com.example.dto.AlbumCommentReactionRequest;
import com.example.dto.AlbumCommentRequest;
import com.example.dto.AlbumDto;
import com.example.dto.SaveAlbumDto;
import com.example.rest.service.security.SpringUser;
import com.example.service.AlbumCommentReactionService;
import com.example.service.AlbumCommentService;
import com.example.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumEndpoint {
    private final AlbumService albumService;
    private final AlbumCommentService albumCommentService;
    private final AlbumCommentReactionService albumCommentReactionService;

    @GetMapping
    public Page<AlbumDto> getAlbums(@PageableDefault Pageable pageable) {
        return albumService.findAllDto(pageable);
    }

    @GetMapping("/{id}")
    public AlbumDto getAlbum(@PathVariable Long id) {
        return albumService.findAlbumDtoById(id);
    }

    @GetMapping("/{albumId}/comments")
    public Page<AlbumCommentDto> viewComments(@PageableDefault Pageable pageable, @PathVariable Long albumId) {
        return albumCommentService.findAllDto(pageable, albumId);
    }

    @GetMapping("/comments/{id}")
    public AlbumCommentDto viewComment(@PathVariable Long id) {
        return albumCommentService.getAlbumComment(id);
    }

    @PostMapping("/comments")
    public AlbumCommentDto saveComment(@RequestBody @Valid AlbumCommentRequest request, @AuthenticationPrincipal SpringUser user) {
        return albumCommentService.createAlbumComment(request, user.getUser());
    }

    @PostMapping("/comments/rate")
    public AlbumCommentDto saveReaction(@RequestBody @Valid AlbumCommentReactionRequest request, @AuthenticationPrincipal SpringUser user) {
        albumCommentReactionService.saveCommentReaction(request, user.getUser());
        return viewComment(request.getCommentId());
    }

    @PostMapping
    @Operation(summary = "Save new album")
    public ResponseEntity<AlbumDto> saveAlbum(@RequestBody @Valid SaveAlbumDto saveAlbumDto) {
        AlbumDto albumDto = albumService.saveDto(saveAlbumDto);
        URI location = URI.create("/albums/" + albumDto.getId());
        return ResponseEntity.created(location).body(albumDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public AlbumDto updateAlbum(@PathVariable Long id, @RequestBody @Valid SaveAlbumDto dto) {
        return albumService.updateAlbumDto(dto, id);
    }
}
