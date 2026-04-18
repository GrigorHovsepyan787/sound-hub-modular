package com.example.service.impl;

import com.example.dto.SearchResult;
import com.example.mapper.SongMapper;
import com.example.repository.AlbumRepository;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongRepository;
import com.example.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final BandRepository bandRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongMapper songMapper;

    @Override
    public SearchResult search(String query, Pageable pageable) {
        return new SearchResult(songRepository.findByTitleContainingIgnoreCase(query, pageable).map(songMapper :: toDto),
                albumRepository.findByTitleContainingIgnoreCase(query, pageable),
                artistRepository.findByNicknameContainingIgnoreCase(query, pageable),
                bandRepository.findByNameContainingIgnoreCase(query, pageable));
    }
}
