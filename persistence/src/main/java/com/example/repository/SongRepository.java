package com.example.repository;

import com.example.model.Genre;
import com.example.model.Song;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    Page<Song> findAll(Specification<Song> spec, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    void incrementPlayCount(@Param("id") Long id);

    Page<Song> findByGenre(Genre genre, Pageable pageable);

    List<Song> findByArtistId(Long artistId);

    List<Song> findTop5ByArtistIdOrderByPlayCountDesc(Long artistId);

    List<Song> findTop5ByBandIdOrderByPlayCountDesc(Long bandId);

    List<Song> findByAlbumId(Long albumId);

}