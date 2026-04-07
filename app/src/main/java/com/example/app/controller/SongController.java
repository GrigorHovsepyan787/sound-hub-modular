package com.example.app.controller;

import com.example.dto.SongDto;
import com.example.model.Genre;
import com.example.model.Song;
import com.example.service.AlbumService;
import com.example.service.ArtistService;
import com.example.service.BandService;
import com.example.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final ArtistService artistService;
    private final BandService bandService;
    private final AlbumService albumService;

    @GetMapping("/songs")
    public String songs(ModelMap modelMap,
                        @RequestParam(required = false) Genre genre,
                        @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC)
                        Pageable pageable) {

        Page<SongDto> songs = songService.findSongsByGenre(genre, pageable);

        modelMap.addAttribute("songs", songs);
        modelMap.addAttribute("genres", Genre.values());
        modelMap.addAttribute("selectedGenre", genre);
        modelMap.addAttribute("pageNumbers", songService.getPageNumbers(songs));
        modelMap.addAttribute("currentSort",
                pageable.getSort().stream()
                        .findFirst()
                        .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                        .orElse("id,desc")
        );

        return "songs";
    }

    @PostMapping("/songs")
    public String addSong(@ModelAttribute Song song, @RequestParam("songUrl") MultipartFile multipartFile) {
        songService.save(song, multipartFile);
        return "redirect:/songs";
    }

    @GetMapping("/songs/add")
    public String addSong(ModelMap modelMap) {
        modelMap.addAttribute("genres", Genre.values());
        modelMap.addAttribute("artists", artistService.findAll());
        modelMap.addAttribute("bands", bandService.findAll());
        modelMap.addAttribute("albums", albumService.findAll());
        return "addSong";
    }

    @GetMapping("/songs/delete")
    public String deleteSong(@RequestParam("id") Long id) {
        songService.delete(id);
        return "redirect:/songs";
    }

    @GetMapping("/songs/play")
    public String registerPlay(@RequestParam("id") Long id) {
        songService.incrementPlayCount(id);
        return "redirect:/songs";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("performer_type", "has_album", "songUrl");
    }
}