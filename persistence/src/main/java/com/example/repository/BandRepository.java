package com.example.repository;

import com.example.model.Band;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;

public interface BandRepository extends JpaRepository<Band, Long>, JpaSpecificationExecutor<Band> {

    @Transactional
    @Modifying
    Page<Band> findAll(Specification<Band> spec, Pageable pageable);

}