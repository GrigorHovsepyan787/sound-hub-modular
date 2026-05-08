package com.example.rest.endpoint;

import com.example.dto.BandDto;
import com.example.dto.SongDto;
import com.example.service.BandService;
import com.example.service.SongService;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bands")
@RequiredArgsConstructor
public class BandEndpoint {

    private final BandService bandService;
    private final SongService songService;

    @GetMapping
    public ResponseEntity<Page<BandDto>> getBands(
            @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bandService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BandDto> getBand(@PathVariable Long id) {
        return ResponseEntity.ok(bandService.getBandById(id));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<BandPreviewResponse> getBandPreview(@PathVariable Long id) {
        BandDto band = bandService.getBandByIdForArtists(id);
        List<SongDto> songs = songService.getTop5SongsOfBandByPlayCount(id);
        return ResponseEntity.ok(new BandPreviewResponse(band, songs));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<BandDto> createBand(
            @RequestPart("band") BandDto bandDto,
            @RequestPart(value = "bandImage", required = false) MultipartFile bandImage) {
        return ResponseEntity.status(201).body(bandService.create(bandDto, bandImage));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<BandDto> updateBand(
            @PathVariable Long id,
            @RequestPart("band") BandDto bandDto,
            @RequestPart(value = "bandImage", required = false) MultipartFile bandImage) {
        return ResponseEntity.ok(bandService.update(id, bandDto, bandImage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBand(@PathVariable Long id) {
        bandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record BandPreviewResponse(BandDto band, List<SongDto> songs) {}
}