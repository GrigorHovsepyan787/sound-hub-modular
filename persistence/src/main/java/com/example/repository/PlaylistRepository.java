package com.example.repository;

import com.example.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>{

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.id = :id")
    Optional<Playlist> findByIdWithSongs(@Param("id") Long id);

    List<Playlist> findByUserId(Long userId);
}