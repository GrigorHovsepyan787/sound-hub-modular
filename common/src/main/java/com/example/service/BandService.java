package com.example.service;

import com.example.model.Band;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BandService {

    Page<Band> findAll(Pageable pageable);

    Band create(Band band, MultipartFile multipartFile);

    Band update(Long id, Band Band);

    void delete(Long id);

    Band getBandById(Long id);

}
