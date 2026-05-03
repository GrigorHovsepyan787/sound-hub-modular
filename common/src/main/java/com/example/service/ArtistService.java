package com.example.service;

import com.example.dto.ArtistDto;
import com.example.projection.ArtistPopularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistService {

    Page<ArtistDto> findAll(Pageable pageable);

    List<ArtistDto> findAll();

    ArtistDto save(ArtistDto artistDto, MultipartFile multipartFile, List<Long> bandIds);

    ArtistDto update(Long id, ArtistDto artistDto, MultipartFile multipartFile, List<Long> bandIds);

    void delete(Long id);

    ArtistDto getArtistById(Long id);

    Page<ArtistDto> getArtistsByName(String name, Pageable pageable);

    Page<ArtistPopularity> getTopArtistPopularityLastMonth(Pageable pageable);
}