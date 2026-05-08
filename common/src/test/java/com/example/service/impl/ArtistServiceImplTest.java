package com.example.service.impl;

import com.example.dto.ArtistDto;
import com.example.mapper.ArtistMapper;
import com.example.model.Artist;
import com.example.model.Band;
import com.example.projection.ArtistPopularity;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock private ArtistRepository artistRepository;
    @Mock private BandRepository bandRepository;
    @Mock private StorageService storageService;
    @Mock private SongPlayRepository songPlayRepository;
    @Mock private ArtistMapper artistMapper;

    @InjectMocks private ArtistServiceImpl artistService;

    @Test
    void findAll_withPageable_returnsPageOfDtos() {
        Pageable pageable = PageRequest.of(0, 5);
        Artist artist = new Artist();
        ArtistDto dto = new ArtistDto();
        Page<Artist> artistPage = new PageImpl<>(List.of(artist));
        when(artistRepository.findAll(pageable)).thenReturn(artistPage);
        when(artistMapper.toDto(artist)).thenReturn(dto);

        Page<ArtistDto> result = artistService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void findAll_noArgs_returnsListOfDtos() {
        Artist artist = new Artist();
        ArtistDto dto = new ArtistDto();
        when(artistRepository.findAll()).thenReturn(List.of(artist));
        when(artistMapper.toDto(artist)).thenReturn(dto);

        List<ArtistDto> result = artistService.findAll();

        assertThat(result).containsExactly(dto);
    }

    @Test
    void save_withFileAndBandIds_uploadsImageAndSetsBands() {
        ArtistDto dto = new ArtistDto();
        Artist artist = new Artist();
        artist.setBands(new HashSet<>());
        ArtistDto savedDto = new ArtistDto();
        MultipartFile file = mock(MultipartFile.class);
        Band band = new Band();

        when(file.isEmpty()).thenReturn(false);
        when(artistMapper.toEntity(dto)).thenReturn(artist);
        when(storageService.upload(file, "artist-images")).thenReturn("http://img.url");
        when(bandRepository.findById(1L)).thenReturn(Optional.of(band));
        when(artistRepository.save(artist)).thenReturn(artist);
        when(artistMapper.toDto(artist)).thenReturn(savedDto);

        ArtistDto result = artistService.save(dto, file, List.of(1L));

        assertThat(artist.getPictureUrl()).isEqualTo("http://img.url");
        assertThat(artist.getBands()).contains(band);
        assertThat(result).isEqualTo(savedDto);
    }

    @Test
    void save_withNullFile_usesDefaultImage() {
        ArtistDto dto = new ArtistDto();
        Artist artist = new Artist();
        when(artistMapper.toEntity(dto)).thenReturn(artist);
        when(artistRepository.save(artist)).thenReturn(artist);
        when(artistMapper.toDto(artist)).thenReturn(new ArtistDto());

        artistService.save(dto, null, null);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void save_withNullBandIds_doesNotSetBands() {
        ArtistDto dto = new ArtistDto();
        Artist artist = new Artist();
        when(artistMapper.toEntity(dto)).thenReturn(artist);
        when(artistRepository.save(artist)).thenReturn(artist);
        when(artistMapper.toDto(artist)).thenReturn(new ArtistDto());

        artistService.save(dto, null, null);

        verify(bandRepository, never()).findById(any());
    }

    @Test
    void save_bandNotFound_throwsEntityNotFoundException() {
        ArtistDto dto = new ArtistDto();
        Artist artist = new Artist();
        when(artistMapper.toEntity(dto)).thenReturn(artist);
        when(bandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.save(dto, null, List.of(99L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_withFileAndBands_updatesArtist() {
        Long id = 1L;
        ArtistDto dto = new ArtistDto();
        Artist existing = new Artist();
        existing.setBands(new HashSet<>());
        ArtistDto updatedDto = new ArtistDto();
        MultipartFile file = mock(MultipartFile.class);
        Band band = new Band();

        when(file.isEmpty()).thenReturn(false);
        when(artistRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bandRepository.findById(2L)).thenReturn(Optional.of(band));
        when(storageService.upload(file, "artist-images")).thenReturn("http://new-img.url");
        when(artistRepository.save(existing)).thenReturn(existing);
        when(artistMapper.toDto(existing)).thenReturn(updatedDto);

        ArtistDto result = artistService.update(id, dto, file, List.of(2L));

        assertThat(existing.getPictureUrl()).isEqualTo("http://new-img.url");
        assertThat(existing.getBands()).contains(band);
        assertThat(result).isEqualTo(updatedDto);
        verify(artistMapper).updateEntityFromDto(dto, existing);
    }

    @Test
    void update_artistNotFound_throwsEntityNotFoundException() {
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.update(99L, new ArtistDto(), null, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_withNullFile_doesNotUploadImage() {
        Long id = 1L;
        Artist existing = new Artist();
        existing.setBands(new HashSet<>());
        when(artistRepository.findById(id)).thenReturn(Optional.of(existing));
        when(artistRepository.save(existing)).thenReturn(existing);
        when(artistMapper.toDto(existing)).thenReturn(new ArtistDto());

        artistService.update(id, new ArtistDto(), null, null);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void update_clearsExistingBandsBeforeAddingNew() {
        Long id = 1L;
        Band oldBand = new Band();
        oldBand.setId(10L);
        Band newBand = new Band();
        newBand.setId(20L);
        Artist existing = new Artist();
        Set<Band> bands = new HashSet<>();
        bands.add(oldBand);
        existing.setBands(bands);

        when(artistRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bandRepository.findById(20L)).thenReturn(Optional.of(newBand));
        when(artistRepository.save(existing)).thenReturn(existing);
        when(artistMapper.toDto(existing)).thenReturn(new ArtistDto());

        artistService.update(id, new ArtistDto(), null, List.of(20L));

        assertThat(existing.getBands()).containsOnly(newBand);
    }

    @Test
    void delete_happyPath_deletesById() {
        artistService.delete(1L);

        verify(artistRepository).deleteById(1L);
    }

    @Test
    void getArtistById_exists_returnsDto() {
        Artist artist = new Artist();
        ArtistDto dto = new ArtistDto();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistMapper.toDto(artist)).thenReturn(dto);

        ArtistDto result = artistService.getArtistById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getArtistById_notFound_throwsEntityNotFoundException() {
        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.getArtistById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getArtistsByName_blankName_returnsAllArtists() {
        Pageable pageable = PageRequest.of(0, 10);
        Artist artist = new Artist();
        ArtistDto dto = new ArtistDto();
        when(artistRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(artist)));
        when(artistMapper.toDto(artist)).thenReturn(dto);

        Page<ArtistDto> result = artistService.getArtistsByName("  ", pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(artistRepository).findAll(pageable);
        verify(artistRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }

    @Test
    void getArtistsByName_validName_searchesByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Artist artist = new Artist();
        ArtistDto dto = new ArtistDto();
        when(artistRepository.findByNameContainingIgnoreCase("john", pageable))
                .thenReturn(new PageImpl<>(List.of(artist)));
        when(artistMapper.toDto(artist)).thenReturn(dto);

        Page<ArtistDto> result = artistService.getArtistsByName("john", pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void getTopArtistPopularityLastMonth_happyPath_returnsPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ArtistPopularity> expected = new PageImpl<>(List.of());
        when(songPlayRepository.findTopArtistsForPeriod(any(), any(), eq(pageable))).thenReturn(expected);

        Page<ArtistPopularity> result = artistService.getTopArtistPopularityLastMonth(pageable);

        assertThat(result).isEqualTo(expected);
    }
}