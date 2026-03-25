package com.example.repository;

import com.example.model.Band;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BandRepository extends JpaRepository<Band, Long>, JpaSpecificationExecutor<Band> {

    Page<Band> findAll(Specification<Band> spec, Pageable pageable);

    @Query("SELECT b FROM Band b LEFT JOIN FETCH b.artists WHERE b.id = :id")
    Optional<Band> findByIdWithArtists(@Param("id") Long id);
}