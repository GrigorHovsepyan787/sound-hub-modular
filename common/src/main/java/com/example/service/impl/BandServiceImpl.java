package com.example.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final StorageService storageService;
    private final SongPlayRepository songPlayRepository;
    private static final String DEFAULT_BAND_IMAGE =
            "https://soundhub7.s3.eu-north-1.amazonaws.com/assets/BandDefault.png";
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public Page<Band> findAll(Pageable pageable) {
        log.info("Fetching bands, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return bandRepository.findAll(pageable);
    }

    @Override
    public List<Band> findAll() {
        return bandRepository.findAll();
    }

    @Override
    public Band create(Band band, MultipartFile multipartFile) {
        String imageUrl;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = storageService.upload(multipartFile, "band-images");
        } else {
            imageUrl = DEFAULT_BAND_IMAGE;
        }
        band.setPictureUrl(imageUrl);

        return bandRepository.save(band);
    }

    @Override
    public Band update(Band editedBand, MultipartFile bandImage) {
        Band existingBand = bandRepository.findById(editedBand.getId())
                .orElseThrow(EntityNotFoundException::new);

        existingBand.setName(editedBand.getName());
        existingBand.setBio(editedBand.getBio());

        if (bandImage != null && !bandImage.isEmpty()) {
            String imageUrl = storageService.upload(bandImage, "band-images");
            existingBand.setPictureUrl(imageUrl);
            log.info("Image updated for band ID: {}", editedBand.getId());
        }
        return bandRepository.save(existingBand);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting band ID: {}", id);
        bandRepository.deleteById(id);
    }

    @Override
    public Band getBandById(Long id) {
        log.info("Fetching band ID: {}", id);
        return bandRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Integer> getPageNumbers(Page<Band> bands) {

        int totalPages = bands.getTotalPages();
        log.info("Total pages: {}", totalPages);

        if (totalPages == 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }

    @Override
    public Band getBandByIdForArtists(Long id) {
        log.info("Fetching band ID with artists: {}", id);
        return bandRepository.findByIdWithArtists(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Page<Band> getBandsByName(String name, Pageable pageable) {
        if (StringUtils.isBlank(name)) {
            return bandRepository.findAll(pageable);
        }
        return bandRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<BandPopularity> getTopBandPopularityLastMonth(Pageable pageable) {
        DateRange month = DateRangeUtils.monthlyRange(clock);

        return songPlayRepository.findTopBandsForPeriod(
                month.currentStart(),
                month.currentEnd(),
                pageable);
    }
}
