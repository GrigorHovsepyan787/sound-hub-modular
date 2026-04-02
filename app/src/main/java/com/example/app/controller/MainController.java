package com.example.app.controller;


import com.example.app.service.security.SpringUser;
import com.example.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ArtistService artistService;
    @GetMapping("/")
    public String mainPage(@AuthenticationPrincipal SpringUser springUser,
                           ModelMap modelMap,
                           @PageableDefault(size = 5) Pageable pageable) {
        if (springUser != null) {
            modelMap.addAttribute("user", springUser.getUser());
        }
        modelMap.addAttribute("artists", artistService.getTopArtistPopularityCurrentMonth(pageable));
        return "feed";
    }
}
