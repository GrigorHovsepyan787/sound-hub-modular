package com.example.service.impl;

import com.example.model.Band;
import com.example.repository.BandRepository;
import com.example.service.BandService;
import com.example.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final StorageService storageService;

    @Override
    public Page<Band> findAll(Pageable pageable) {
        log.info("Fetching bands, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return bandRepository.findAll(pageable);
    }

    @Override
    public Band create(Band band, MultipartFile multipartFile) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "band-images");

            if (imageUrl != null) {
                band.setPictureUrl(imageUrl);
                log.info("Image uploaded for band: {}", band.getName());
            }
        }
        return bandRepository.save(band);
    }


    public Band update(Band editedBand, MultipartFile bandImage) {
        Band existingBand =bandRepository.findById(editedBand.getId())
                .orElseThrow(EntityNotFoundException::new);

        existingBand.setName(editedBand.getName());
        existingBand.setBio(editedBand.getBio());

        if (bandImage != null && !bandImage.isEmpty()) {
            String imageUrl = storageService.upload(bandImage, "band-images");

            if (imageUrl != null) {
                existingBand.setPictureUrl(imageUrl);
                log.info("Image updated for band ID: {}", editedBand.getId());
            }
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
        return bandRepository.findById(id).orElse(null);
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
    public Page<Band> getBandsByName(String name, Pageable pageable) {
        if(name == null || name.isEmpty()) {
            return bandRepository.findAll(pageable);
        }
        return bandRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}
