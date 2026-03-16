package com.example.service;

import com.example.model.Band;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BandService {

    Page<Band> findAll(Integer page, Integer size, String sortParam);

    Band create(Band band, MultipartFile multipartFile);

    Band update(Band editedBand, MultipartFile bandImage);

    void delete(Long id);

    Band getBandById(Long id);

    List<Integer> getPageNumbers(Page<Band> bands);
}