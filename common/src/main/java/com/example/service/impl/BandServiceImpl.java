package com.example.service.impl;

import com.example.model.Band;
import com.example.properties.S3Properties;
import com.example.repository.BandRepository;
import com.example.service.BandService;
import com.example.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final StorageService storageService;

    @Override
    public Page<Band> findAll(Integer page, Integer size, String sortParam) {

        int currentPage = page == null ? 1 : page;
        int pageSize = size == null ? 6 : size;

        String sortValue = sortParam == null ? "id,desc" : sortParam;
        String[] sortParts = sortValue.split(",");

        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.DESC;

        if (sortParts.length > 1) {
            direction = Sort.Direction.fromString(sortParts[1]);
        }

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(direction, sortField));

        return bandRepository.findAll(pageable);
    }

    @Override
    public Band create(Band band, MultipartFile multipartFile) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "band-images");

            if (imageUrl != null) {
                band.setPictureName(imageUrl);
            }
        }
        return bandRepository.save(band);
    }


    public Band update(Band editedBand, MultipartFile bandImage) {
        Band existingBand = bandRepository.findById(editedBand.getId()).orElseThrow();

        existingBand.setName(editedBand.getName());
        existingBand.setBio(editedBand.getBio());

        if (bandImage != null && !bandImage.isEmpty()) {
            String imageUrl = storageService.upload(bandImage, "band-images");

            if (imageUrl != null) {
                existingBand.setPictureName(imageUrl);
            }
        }
        return bandRepository.save(existingBand);
    }

    @Override
    public void delete(Long id) {
        bandRepository.deleteById(id);
    }

    @Override
    public Band getBandById(Long id) {
        return bandRepository.findById(id).orElse(null);
    }

    @Override
    public List<Integer> getPageNumbers(Page<Band> bands) {

        int totalPages = bands.getTotalPages();

        if (totalPages == 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
    }
}
