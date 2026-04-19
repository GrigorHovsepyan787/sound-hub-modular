package com.example.service;

import com.example.dto.SearchResult;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    SearchResult search(String query,  Pageable pageable);
}
