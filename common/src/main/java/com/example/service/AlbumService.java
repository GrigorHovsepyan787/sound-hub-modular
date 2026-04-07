package com.example.service;

import com.example.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AlbumService {
    Page<Album> findAlbumPage(Pageable pageable);

    List<Album> findAll();

    void save(Album album, MultipartFile multipartFile, Long bandId, Long artistId);

    Album findAlbumById(Long id);

    void update(Album album, MultipartFile multipartFile, Long bandId, Long artistId);
}
