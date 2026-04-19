package com.example.repository;

import com.example.model.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface ArtistRepository extends JpaRepository<Artist, Long>, JpaSpecificationExecutor<Artist> {

    Page<Artist> findAll(Specification<Artist> spec, Pageable pageable);

    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Artist> findByNicknameContainingIgnoreCase(String name, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}