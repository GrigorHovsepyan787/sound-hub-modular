package com.example.rest.endpoint;

import com.example.dto.ArtistDto;
import com.example.dto.SongDto;
import com.example.service.ArtistService;
import com.example.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "Artists management endpoints")
public class ArtistEndpoint {

    private final ArtistService artistService;
    private final SongService songService;

    @Operation(
            summary = "Get all artists",
            description = "Returns paginated list of artists"
    )
    @GetMapping
    public ResponseEntity<Page<ArtistDto>> getArtists(
            @ParameterObject @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(artistService.findAll(pageable));
    }

    @Operation(
            summary = "Get artist by id",
            description = "Returns artist details by id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ArtistDto> getArtist(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @Operation(
            summary = "Preview artist by id",
            description = "Returns artist details and songs by id"
    )
    @GetMapping("/{id}/preview")
    public ResponseEntity<ArtistPreviewResponse> getArtistPreview(@PathVariable Long id) {
        ArtistDto artist = artistService.getArtistById(id);
        List<SongDto> songs = songService.getTop5SongsOfArtistByPlayCount(id);
        return ResponseEntity.ok(new ArtistPreviewResponse(artist, songs));
    }

    @Operation(
            summary = "Create artist",
            description = "Create artist object"
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ArtistDto> createArtist(
            @RequestPart("artist") ArtistDto artistDto,
            @RequestPart(value = "artistImage", required = false) MultipartFile artistImage,
            @RequestParam(value = "bandIds", required = false) List<Long> bandIds) {
        return ResponseEntity.status(201).body(artistService.save(artistDto, artistImage, bandIds));
    }

    @Operation(
            summary = "Update artist",
            description = "Update artist object"
    )
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ArtistDto> updateArtist(
            @PathVariable Long id,
            @RequestPart("artist") ArtistDto artistDto,
            @RequestPart(value = "artistImage", required = false) MultipartFile artistImage,
            @RequestParam(value = "bandIds", required = false) List<Long> bandIds) {
        return ResponseEntity.ok(artistService.update(id, artistDto, artistImage, bandIds));
    }

    @Operation(
            summary = "Delete artist",
            description = "Delete artist by id"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record ArtistPreviewResponse(ArtistDto artist, List<SongDto> songs) {}
}