package com.example.app.controller;

import com.example.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/fragment")
    public String searchFragment(@RequestParam String query,
                                 @PageableDefault(size = 3) Pageable pageable,
                                 ModelMap modelMap) {
        modelMap.addAttribute("result", searchService.search(query, pageable));
        System.out.println(searchService.search(query, pageable));
        return "fragments/dropdown :: dropdown";
    }
}
