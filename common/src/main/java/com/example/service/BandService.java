package com.example.service;

import com.example.dto.BandDto;
import com.example.projection.BandPopularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BandService {

    Page<BandDto> findAll(Pageable pageable);

    List<BandDto> findAll();

    BandDto create(BandDto bandDto, MultipartFile multipartFile);

    BandDto update(Long id, BandDto bandDto, MultipartFile bandImage);

    void delete(Long id);

    BandDto getBandById(Long id);

    Page<BandDto> getBandsByName(String name, Pageable pageable);

    BandDto getBandByIdForArtists(Long id);

    Page<BandPopularity> getTopBandPopularityLastMonth(Pageable pageable);
}