package com.example.app.controller;

import com.example.dto.BandDto;
import com.example.dto.SongDto;
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
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class BandController {

    private final BandService bandService;
    private final SongService songService;

    @GetMapping("/bands")
    public String bands(ModelMap modelMap,
                        @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC)
                        Pageable pageable) {

        Page<BandDto> bands = bandService.findAll(pageable);

        modelMap.addAttribute("bands", bands);
        modelMap.addAttribute("pageNumbers", getPageNumbers(bands));
        modelMap.addAttribute("currentSort",
                pageable.getSort().stream()
                        .findFirst()
                        .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                        .orElse("id,desc")
        );

        return "bands";
    }

    @PostMapping("/bands")
    public String addBand(@ModelAttribute BandDto bandDto, @RequestParam("bandImage") MultipartFile bandImage) {
        bandService.create(bandDto, bandImage);
        return "redirect:/bands";
    }

    @GetMapping("/bands/add")
    public String addBand() {
        return "addBand";
    }

    @GetMapping("/bands/edit")
    public String editBand(@RequestParam("id") Long id, ModelMap modelMap) {
        BandDto band = bandService.getBandById(id);
        modelMap.addAttribute("band", band);
        return "editBand";
    }

    @PostMapping("/bands/edit")
    public String editBand(@RequestParam("id") Long id,
                           @ModelAttribute BandDto bandDto,
                           @RequestParam("bandImage") MultipartFile bandImage) {
        bandService.update(id, bandDto, bandImage);
        return "redirect:/bands";
    }

    @GetMapping("/bands/delete")
    public String deleteBand(@RequestParam("id") Long id) {
        bandService.delete(id);
        return "redirect:/bands";
    }

    @GetMapping("/bands/preview")
    public String bandPreviewPage(@RequestParam("id") Long id, ModelMap modelMap) {
        BandDto band = bandService.getBandByIdForArtists(id);
        List<SongDto> songs = songService.getTop5SongsOfBandByPlayCount(id);
        modelMap.addAttribute("band", band);
        modelMap.addAttribute("songs", songs);
        return "bandPreview";
    }

    private List<Integer> getPageNumbers(Page<?> page) {
        int totalPages = page.getTotalPages();
        if (totalPages == 0) {
            return List.of();
        }
        return IntStream.rangeClosed(1, totalPages).boxed().toList();
    }
}