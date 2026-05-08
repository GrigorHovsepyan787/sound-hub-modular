package com.example.service;

import com.example.dto.AlbumDto;
import com.example.dto.SaveAlbumDto;
import com.example.model.Album;
import com.example.projection.AlbumPopularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AlbumService {
    Page<Album> findAlbumPage(Pageable pageable);

    List<Album> findAll();

    Page<AlbumDto> findAllDto(Pageable pageable);

    void save(Album album, MultipartFile multipartFile, Long bandId, Long artistId);

    AlbumDto saveDto(SaveAlbumDto saveAlbumDto);

    Album findAlbumById(Long id);

    AlbumDto findAlbumDtoById(Long id);

    void update(Album album, MultipartFile multipartFile, Long bandId, Long artistId);

    Page<AlbumPopularity> getTopAlbumPopularityLastMonth(Pageable pageable);

    Page<Album> findByArtistIsNotNull(Pageable pageable);

    Page<Album> findByBandIsNotNull(Pageable pageable);

    void delete(Long id);

    AlbumDto updateAlbumDto(SaveAlbumDto dto, Long id);
}
