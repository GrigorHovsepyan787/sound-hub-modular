package com.example.service.impl;

import com.example.dto.BandDto;
import com.example.mapper.BandMapper;
import com.example.model.Band;
import com.example.projection.BandPopularity;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BandServiceImplTest {

    @Mock private BandRepository bandRepository;
    @Mock private StorageService storageService;
    @Mock private SongPlayRepository songPlayRepository;
    @Mock private BandMapper bandMapper;

    @InjectMocks private BandServiceImpl bandService;

    @Test
    void findAll_withPageable_returnsPageOfDtos() {
        Pageable pageable = PageRequest.of(0, 5);
        Band band = new Band();
        BandDto dto = new BandDto();
        when(bandRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(band)));
        when(bandMapper.toDto(band)).thenReturn(dto);

        Page<BandDto> result = bandService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void findAll_noArgs_returnsListOfDtos() {
        Band band = new Band();
        BandDto dto = new BandDto();
        when(bandRepository.findAll()).thenReturn(List.of(band));
        when(bandMapper.toDto(band)).thenReturn(dto);

        List<BandDto> result = bandService.findAll();

        assertThat(result).containsExactly(dto);
    }

    @Test
    void create_withFile_uploadsImageAndSaves() {
        BandDto dto = new BandDto();
        Band band = new Band();
        BandDto savedDto = new BandDto();
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(bandMapper.toEntity(dto)).thenReturn(band);
        when(storageService.upload(file, "band-images")).thenReturn("http://img.url");
        when(bandRepository.save(band)).thenReturn(band);
        when(bandMapper.toDto(band)).thenReturn(savedDto);

        BandDto result = bandService.create(dto, file);

        assertThat(band.getPictureUrl()).isEqualTo("http://img.url");
        assertThat(result).isEqualTo(savedDto);
        verify(bandRepository).save(band);
    }

    @Test
    void create_withNullFile_usesDefaultImage() {
        BandDto dto = new BandDto();
        Band band = new Band();
        when(bandMapper.toEntity(dto)).thenReturn(band);
        when(bandRepository.save(band)).thenReturn(band);
        when(bandMapper.toDto(band)).thenReturn(new BandDto());

        bandService.create(dto, null);

        verify(storageService, never()).upload(any(), any());
        verify(bandRepository).save(band);
    }

    @Test
    void create_withEmptyFile_usesDefaultImage() {
        BandDto dto = new BandDto();
        Band band = new Band();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(bandMapper.toEntity(dto)).thenReturn(band);
        when(bandRepository.save(band)).thenReturn(band);
        when(bandMapper.toDto(band)).thenReturn(new BandDto());

        bandService.create(dto, file);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void update_withFile_updatesImageAndSaves() {
        Long id = 1L;
        BandDto dto = new BandDto();
        Band existing = new Band();
        BandDto updatedDto = new BandDto();
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(bandRepository.findById(id)).thenReturn(Optional.of(existing));
        when(storageService.upload(file, "band-images")).thenReturn("http://new-img.url");
        when(bandRepository.save(existing)).thenReturn(existing);
        when(bandMapper.toDto(existing)).thenReturn(updatedDto);

        BandDto result = bandService.update(id, dto, file);

        assertThat(existing.getPictureUrl()).isEqualTo("http://new-img.url");
        assertThat(result).isEqualTo(updatedDto);
        verify(bandMapper).updateEntityFromDto(dto, existing);
    }

    @Test
    void update_withNullFile_doesNotUploadImage() {
        Long id = 1L;
        Band existing = new Band();
        when(bandRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bandRepository.save(existing)).thenReturn(existing);
        when(bandMapper.toDto(existing)).thenReturn(new BandDto());

        bandService.update(id, new BandDto(), null);

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    void update_bandNotFound_throwsEntityNotFoundException() {
        when(bandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bandService.update(99L, new BandDto(), null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_happyPath_deletesById() {
        bandService.delete(1L);

        verify(bandRepository).deleteById(1L);
    }

    @Test
    void getBandById_exists_returnsDto() {
        Band band = new Band();
        BandDto dto = new BandDto();
        when(bandRepository.findById(1L)).thenReturn(Optional.of(band));
        when(bandMapper.toDto(band)).thenReturn(dto);

        BandDto result = bandService.getBandById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getBandById_notFound_throwsEntityNotFoundException() {
        when(bandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bandService.getBandById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getBandByIdForArtists_exists_returnsDto() {
        Band band = new Band();
        BandDto dto = new BandDto();
        when(bandRepository.findByIdWithArtists(1L)).thenReturn(Optional.of(band));
        when(bandMapper.toDto(band)).thenReturn(dto);

        BandDto result = bandService.getBandByIdForArtists(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getBandByIdForArtists_notFound_throwsEntityNotFoundException() {
        when(bandRepository.findByIdWithArtists(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bandService.getBandByIdForArtists(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getBandsByName_blankName_returnsAllBands() {
        Pageable pageable = PageRequest.of(0, 10);
        Band band = new Band();
        BandDto dto = new BandDto();
        when(bandRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(band)));
        when(bandMapper.toDto(band)).thenReturn(dto);

        Page<BandDto> result = bandService.getBandsByName("", pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(bandRepository).findAll(pageable);
        verify(bandRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }

    @Test
    void getBandsByName_validName_searchesByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Band band = new Band();
        BandDto dto = new BandDto();
        when(bandRepository.findByNameContainingIgnoreCase("Metallica", pageable))
                .thenReturn(new PageImpl<>(List.of(band)));
        when(bandMapper.toDto(band)).thenReturn(dto);

        Page<BandDto> result = bandService.getBandsByName("Metallica", pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void getTopBandPopularityLastMonth_happyPath_returnsPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<BandPopularity> expected = new PageImpl<>(List.of());
        when(songPlayRepository.findTopBandsForPeriod(any(), any(), eq(pageable))).thenReturn(expected);

        Page<BandPopularity> result = bandService.getTopBandPopularityLastMonth(pageable);

        assertThat(result).isEqualTo(expected);
    }
}