package com.example.dto;

import com.example.model.Album;
import com.example.model.Artist;
import com.example.model.Band;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SearchResult {
    private Page<SongDto> songs;
    private Page<Album> albums;
    private Page<Artist> artists;
    private Page<Band> bands;
}
