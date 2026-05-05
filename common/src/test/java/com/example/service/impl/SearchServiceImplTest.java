package com.example.service.impl;

import com.example.dto.SearchResult;
import com.example.dto.SongDto;
import com.example.mapper.SongMapper;
import com.example.model.Album;
import com.example.model.Artist;
import com.example.model.Band;
import com.example.model.Song;
import com.example.repository.AlbumRepository;
import com.example.repository.ArtistRepository;
import com.example.repository.BandRepository;
import com.example.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock private BandRepository bandRepository;
    @Mock private SongRepository songRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private AlbumRepository albumRepository;
    @Mock private SongMapper songMapper;

    @InjectMocks private SearchServiceImpl searchService;

    @Test
    void search_happyPath_returnsSearchResultWithAllEntities() {
        String query = "rock";
        Pageable pageable = PageRequest.of(0, 10);

        Song song = new Song();
        SongDto songDto = new SongDto();
        Page<Song> songPage = new PageImpl<>(List.of(song));
        Page<Album> albumPage = new PageImpl<>(List.of(new Album()));
        Page<Artist> artistPage = new PageImpl<>(List.of(new Artist()));
        Page<Band> bandPage = new PageImpl<>(List.of(new Band()));

        when(songRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(songPage);
        when(songMapper.toDto(song)).thenReturn(songDto);
        when(albumRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(albumPage);
        when(artistRepository.findByNicknameContainingIgnoreCase(query, pageable)).thenReturn(artistPage);
        when(bandRepository.findByNameContainingIgnoreCase(query, pageable)).thenReturn(bandPage);

        SearchResult result = searchService.search(query, pageable);

        assertThat(result).isNotNull();
        verify(songRepository).findByTitleContainingIgnoreCase(query, pageable);
        verify(albumRepository).findByTitleContainingIgnoreCase(query, pageable);
        verify(artistRepository).findByNicknameContainingIgnoreCase(query, pageable);
        verify(bandRepository).findByNameContainingIgnoreCase(query, pageable);
    }

    @Test
    void search_emptyQuery_delegatesToRepositoriesWithEmptyString() {
        String query = "";
        Pageable pageable = PageRequest.of(0, 10);

        when(songRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(albumRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(artistRepository.findByNicknameContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(bandRepository.findByNameContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());

        SearchResult result = searchService.search(query, pageable);

        assertThat(result).isNotNull();
    }

    @Test
    void search_noResults_returnsEmptyPages() {
        String query = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);

        when(songRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(albumRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(artistRepository.findByNicknameContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(bandRepository.findByNameContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());

        SearchResult result = searchService.search(query, pageable);

        assertThat(result).isNotNull();
        verify(songRepository).findByTitleContainingIgnoreCase(query, pageable);
        verify(albumRepository).findByTitleContainingIgnoreCase(query, pageable);
        verify(artistRepository).findByNicknameContainingIgnoreCase(query, pageable);
        verify(bandRepository).findByNameContainingIgnoreCase(query, pageable);
    }

    @Test
    void search_songMapperInvoked_forEachSongInPage() {
        String query = "pop";
        Pageable pageable = PageRequest.of(0, 5);

        Song song1 = new Song();
        Song song2 = new Song();
        SongDto dto1 = new SongDto();
        SongDto dto2 = new SongDto();

        when(songRepository.findByTitleContainingIgnoreCase(query, pageable))
                .thenReturn(new PageImpl<>(List.of(song1, song2)));
        when(songMapper.toDto(song1)).thenReturn(dto1);
        when(songMapper.toDto(song2)).thenReturn(dto2);
        when(albumRepository.findByTitleContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(artistRepository.findByNicknameContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());
        when(bandRepository.findByNameContainingIgnoreCase(query, pageable)).thenReturn(Page.empty());

        searchService.search(query, pageable);

        verify(songMapper).toDto(song1);
        verify(songMapper).toDto(song2);
    }
}