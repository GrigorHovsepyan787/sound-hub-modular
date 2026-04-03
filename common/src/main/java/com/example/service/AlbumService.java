package com.example.service;

import com.example.model.Album;
import com.example.projection.AlbumPopularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AlbumService {
    Page<Album> findAlbumPage(Pageable pageable);

    void save(Album album, MultipartFile multipartFile, Long bandId, Long artistId);

    Album findAlbumById(Long id);

    void update(Album album, MultipartFile multipartFile, Long bandId, Long artistId);

    Page<AlbumPopularity> getTopAlbumPopularityCurrentMonth(Pageable pageable);

    Page<Album> findByArtistIsNotNull(Pageable pageable);

    Page<Album> findByBandIsNotNull(Pageable pageable);
}
