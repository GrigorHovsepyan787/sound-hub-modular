package com.example.repository;

import com.example.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findAll(Specification<Album> spec, Pageable pageable);

    Page<Album> findByArtistIsNotNull(Pageable pageable);

    Page<Album> findByBandIsNotNull(Pageable pageable);

    Page<Album> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
