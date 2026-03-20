package com.example.service.impl;


import com.example.model.Album;
import com.example.repository.AlbumRepository;
import com.example.service.AlbumService;
import com.example.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {
    private final AlbumRepository albumRepository;
    private final StorageService storageService;

    @Override
    public Page<Album> findAlbumPage(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    @Override
    public void save(Album album, MultipartFile multipartFile) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "album-images");

            if (imageUrl != null) {
                album.setPictureUrl(imageUrl);
                log.info("Image uploaded for album: {}", album.getTitle());
            }
        }
        albumRepository.save(album);
    }
}

