package com.example.app.controller;


import com.example.app.service.security.SpringUser;
import com.example.service.AlbumService;
import com.example.service.ArtistService;
import com.example.service.BandService;
import com.example.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ArtistService artistService;
    private final BandService bandService;
    private final AlbumService albumService;
    private final SongService songService;
    @Value("${hero-banner.image-url}")
    private String HERO_BANNER_URL;

    @GetMapping("/")
    public String mainPage(@AuthenticationPrincipal SpringUser springUser,
                           ModelMap modelMap,
                           @PageableDefault(size = 5, sort = "totalPlays", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable recentPageable = PageRequest.of(0, 5, Sort.by("releaseDate").descending());
        if (springUser != null) {
            modelMap.addAttribute("user", springUser.getUser());
        }
        modelMap.addAttribute("artists", artistService.getTopArtistPopularityLastMonth(pageable));
        modelMap.addAttribute("bands", bandService.getTopBandPopularityLastMonth(pageable));
        modelMap.addAttribute("albums", albumService.getTopAlbumPopularityLastMonth(pageable));
        modelMap.addAttribute("songs", songService.getTopSongPopularityLastMonth(pageable));
        modelMap.addAttribute("recentArtistAlbums", albumService.findByArtistIsNotNull(recentPageable));
        modelMap.addAttribute("recentBandAlbums", albumService.findByBandIsNotNull(recentPageable));
        modelMap.addAttribute("bannerUrl", HERO_BANNER_URL);
        return "feed";
    }
}
