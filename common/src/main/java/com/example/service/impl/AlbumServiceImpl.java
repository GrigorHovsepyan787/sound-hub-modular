package com.example.service.impl;


import com.example.model.Album;
import com.example.repository.AlbumRepository;
import com.example.repository.BandRepository;
import com.example.service.AlbumService;
import com.example.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final BandRepository bandRepository;
//    private final ArtistRepository artistRepository;

    @Override
    public Page<Album> findAlbumPage(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    @Override
    public void save(Album album, @RequestParam("pic") MultipartFile multipartFile, Long bandId, Long artistId) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imageUrl = storageService.upload(multipartFile, "album-images");

            if (imageUrl != null) {
                album.setPictureUrl(imageUrl);
                log.info("Image uploaded for album: {}", album.getTitle());
            }else{
                album.setPictureUrl("https://soundhub7.s3.eu-north-1.amazonaws.com/assets/AlbumDefault.png");
            }
        }
        if (bandId != null) {
            album.setBand(bandRepository.findById(bandId).orElseThrow());
        }
        if (artistId != null) {
//        album.setArtist(artistRepository.findById(artistId).orElseThrow());
        }
        albumRepository.save(album);
    }

    @Override
    public Album findAlbumById(Long id) {
        return albumRepository.findById(id).orElse(null);
    }

    @Override
    public void update(Album album, MultipartFile multipartFile) {

    }
}

