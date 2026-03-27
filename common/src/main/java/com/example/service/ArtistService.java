package com.example.service;

import com.example.model.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistService {

    Page<Artist> findAll(Pageable pageable);

    Artist save(Artist artist, MultipartFile multipartFile, List<Long> bandIds);

    Artist update(Artist editedArtist, MultipartFile multipartFile, List<Long> bandIds);

    void delete(Long id);

    Artist getArtistById(Long id);

    List<Integer> getPageNumbers(Page<Artist> artists);

    Page<Artist> getArtistsByName(String name, Pageable pageable);
}