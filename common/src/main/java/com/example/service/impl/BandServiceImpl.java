package com.example.service.impl;

import com.example.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.model.Band;
import com.example.repository.BandRepository;
import com.example.service.BandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Page<Band> findAll(Pageable pageable) {
        return bandRepository.findAll(pageable);
    }

    @Override
    public Band create(Band band, MultipartFile multipartFile) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                long timestamp = System.currentTimeMillis();
                String fileName = String.valueOf(timestamp);
                String key = s3Properties.getFolders().get("band-images") + "/" + fileName;

                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(s3Properties.getBucket())
                                .key(key)
                                .build(),
                        RequestBody.fromBytes(multipartFile.getBytes())
                );
                String s3Url = "https://" + s3Properties.getBucket() + ".s3." + s3Properties.getRegion() + ".amazonaws.com/" + key;
                band.setPictureName(s3Url);
            } catch (IOException e) {
                log.error("Error uploading file to S3", e);
            }
        }
        return bandRepository.save(band);
    }

    @Override
    public Band update(Long id, Band updatedBand) {
        Band existingBand = bandRepository.findById(id).orElse(null);
        existingBand.setName(updatedBand.getName());
        existingBand.setBio(updatedBand.getBio());
        existingBand.setPictureName(updatedBand.getPictureName());
        existingBand.setCreatedDate(updatedBand.getCreatedDate());
        return bandRepository.save(existingBand);    }

    @Override
    public void delete(Long id) {
        bandRepository.deleteById(id);
    }

    @Override
    public Band getBandById(Long id) {
        return bandRepository.findById(id).orElse(null);
    }
}
