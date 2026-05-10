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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@Tag(name = "Albums", description = "Album management endpoints")
public class AlbumEndpoint {
    private final AlbumService albumService;
    private final AlbumCommentService albumCommentService;
    private final AlbumCommentReactionService albumCommentReactionService;

    @Operation(
            summary = "Get all albums",
            description = "Returns paginated list of albums"
    )
    @GetMapping
    public Page<AlbumDto> getAlbums(@ParameterObject @PageableDefault Pageable pageable) {
        return albumService.findAllDto(pageable);
    }

    @Operation(
            summary = "Get album by id",
            description = "Returns album details by album id"
    )
    @GetMapping("/{id}")
    public AlbumDto getAlbum(@Parameter(description = "Album id") @PathVariable Long id) {
        return albumService.findAlbumDtoById(id);
    }

    @Operation(
            summary = "Get album comments",
            description = "Returns paginated comments for album"
    )
    @GetMapping("/{albumId}/comments")
    public Page<AlbumCommentDto> viewComments(@ParameterObject @PageableDefault Pageable pageable,
                                              @Parameter(description = "Album id") @PathVariable Long albumId) {
        return albumCommentService.findAllDto(pageable, albumId);
    }

    @Operation(
            summary = "Get album comment by id",
            description = "Returns single comment"
    )
    @GetMapping("/comments/{id}")
    public AlbumCommentDto viewComment(@Parameter(description = "Comment id") @PathVariable Long id) {
        return albumCommentService.getAlbumComment(id);
    }

    @Operation(
            summary = "Create new comment",
            description = "Creates comment for album"
    )
    @PostMapping("/comments")
    public ResponseEntity<AlbumCommentDto> saveComment(
            @RequestBody @Valid AlbumCommentRequest request,
            @AuthenticationPrincipal SpringUser user) {
        AlbumCommentDto dto = albumCommentService.createAlbumComment(request, user.getUser());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(
            summary = "Add reaction to comment",
            description = "Adds like/dislike reaction to album comment"
    )
    @PostMapping("/comments/reactions")
    public ResponseEntity<AlbumCommentDto> saveReaction(
            @RequestBody @Valid AlbumCommentReactionRequest request,
            @AuthenticationPrincipal SpringUser user) {
        albumCommentReactionService.saveCommentReaction(request, user.getUser());
        AlbumCommentDto dto = albumCommentService.getAlbumComment(request.getCommentId());

        return ResponseEntity.ok(dto);
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(
            summary = "Create album",
            description = "Creates new album"
    )
    public ResponseEntity<AlbumDto> saveAlbum(@RequestBody @Valid SaveAlbumDto saveAlbumDto,
                                              @RequestPart(value = "albumImage", required = false) MultipartFile albumImage) {
        AlbumDto albumDto = albumService.saveDto(saveAlbumDto, albumImage);
        URI location = URI.create("/api/albums/" + albumDto.getId());
        return ResponseEntity.created(location).body(albumDto);
    }

    @Operation(
            summary = "Delete album",
            description = "Deletes album by id"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(
            @Parameter(description = "Album id")
            @PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update album",
            description = "Updates album by id"
    )
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDto> updateAlbum(
            @Parameter(description = "Album id") @PathVariable Long id,
            @RequestBody @Valid SaveAlbumDto dto,
            @RequestPart(value = "albumImage", required = false) MultipartFile albumImage) {
        AlbumDto updated = albumService.updateAlbumDto(dto, id, albumImage);
        return ResponseEntity.ok(updated);
    }
}
