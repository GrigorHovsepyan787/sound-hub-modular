package com.example.app.controller;

import com.example.dto.ArtistDto;
import com.example.model.Artist;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final BandService bandService;
    private final SongService songService;

    @GetMapping("/artists")
    public String artists(ModelMap modelMap,
                          @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC)
                          Pageable pageable) {

        Page<ArtistDto> artists = artistService.findAll(pageable);

        modelMap.addAttribute("artists", artists);
        modelMap.addAttribute("pageNumbers", artistService.getPageNumbers(artists));
        modelMap.addAttribute("currentSort",
                pageable.getSort().stream()
                        .findFirst()
                        .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                        .orElse("id,desc")
        );

        return "artists";
    }

    @PostMapping("/artists")
    public String addArtist(@ModelAttribute ArtistDto artistDto,
                            @RequestParam(value = "bandIds", required = false) List<Long> bandIds,
                            @RequestParam("artistImage") MultipartFile artistImage) {
        artistService.save(artistDto, artistImage, bandIds);
        return "redirect:/artists";
    }

    @GetMapping("/artists/add")
    public String addArtist(ModelMap modelMap) {
        modelMap.addAttribute("bands", bandService.findAll());
        modelMap.addAttribute("artist", new Artist());
        return "addArtist";
    }

    @GetMapping("/artists/edit")
    public String editArtist(@RequestParam("id") Long id, ModelMap modelMap) {
        modelMap.addAttribute("artist", artistService.getArtistById(id));
        modelMap.addAttribute("bands", bandService.findAll());
        return "editArtist";
    }

    @PostMapping("/artists/edit")
    public String editArtist(@RequestParam("id") Long id,
                             @ModelAttribute ArtistDto artistDto,
                             @RequestParam(value = "bandIds", required = false) List<Long> bandIds,
                             @RequestParam("artistImage") MultipartFile artistImage) {
        artistService.update(id, artistDto, artistImage, bandIds);
        return "redirect:/artists";
    }

    @GetMapping("/artists/delete")
    public String deleteArtist(@RequestParam("id") Long id) {
        artistService.delete(id);
        return "redirect:/artists";
    }

    @GetMapping("/artists/preview")
    public String artistPreviewPage(@RequestParam("id") Long id, ModelMap modelMap) {
        modelMap.addAttribute("artist", artistService.getArtistById(id));
        modelMap.addAttribute("songs", songService.getTop5SongsOfArtistByPlayCount(id));
        return "artistPreview";
    }
}