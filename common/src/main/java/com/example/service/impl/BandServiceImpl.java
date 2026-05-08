package com.example.service.impl;

import com.example.dto.BandDto;
import com.example.mapper.BandMapper;
import com.example.model.Band;
import com.example.projection.BandPopularity;
import com.example.repository.BandRepository;
import com.example.repository.SongPlayRepository;
import com.example.service.BandService;
import com.example.storage.StorageService;
import com.example.util.DateRange;
import com.example.util.DateRangeUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final StorageService storageService;
    private final SongPlayRepository songPlayRepository;
    private final BandMapper bandMapper;
    private final Clock clock = Clock.systemDefaultZone();
    @Value("${band.default-image}")
    private String defaultImageUrl;

    @Override
    public Page<BandDto> findAll(Pageable pageable) {
        log.info("Fetching bands, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return bandRepository.findAll(pageable)
                .map(bandMapper::toDto);
    }

    @Override
    public List<BandDto> findAll() {
        return bandRepository.findAll()
                .stream().map(bandMapper::toDto).toList();
    }

    @Override
    public BandDto create(BandDto bandDto, MultipartFile multipartFile) {
        Band band = bandMapper.toEntity(bandDto);

        String imageUrl;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = storageService.upload(multipartFile, "band-images");
        } else {
            imageUrl = defaultImageUrl;
        }
        band.setPictureUrl(imageUrl);

        return bandMapper.toDto(bandRepository.save(band));
    }

    @Override
    public BandDto update(Long id, BandDto bandDto, MultipartFile bandImage) {
        Band existingBand = bandRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        bandMapper.updateEntityFromDto(bandDto, existingBand);

        if (bandImage != null && !bandImage.isEmpty()) {
            String imageUrl = storageService.upload(bandImage, "band-images");
            existingBand.setPictureUrl(imageUrl);
            log.info("Image updated for band ID: {}", id);
        }
        return bandMapper.toDto(bandRepository.save(existingBand));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting band ID: {}", id);
        bandRepository.deleteById(id);
    }

    @Override
    public BandDto getBandById(Long id) {
        log.info("Fetching band ID: {}", id);
        return bandRepository.findById(id)
                .map(bandMapper::toDto)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public BandDto getBandByIdForArtists(Long id) {
        log.info("Fetching band ID with artists: {}", id);
        return bandRepository.findByIdWithArtists(id)
                .map(bandMapper::toDto)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Page<BandDto> getBandsByName(String name, Pageable pageable) {
        if (StringUtils.isBlank(name)) {
            return bandRepository.findAll(pageable).map(bandMapper::toDto);
        }
        return bandRepository.findByNameContainingIgnoreCase(name, pageable).map(bandMapper::toDto);
    }

    @Override
    public Page<BandPopularity> getTopBandPopularityLastMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.monthlyRange(clock);

        return songPlayRepository.findTopBandsForPeriod(
                month.currentStart(),
                month.currentEnd(),
                pageable);
    }

    @Override
    public List<Integer> getPageNumbers(Page<?> page) {
        int totalPages = page.getTotalPages();
        log.info("Total pages: {}", totalPages);
        if (totalPages == 0) {
            return List.of();
        }
        return IntStream.rangeClosed(1, totalPages).boxed().toList();
    }
}
