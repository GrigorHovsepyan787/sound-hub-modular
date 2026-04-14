package com.example.app.controller;

import com.example.dto.SongDto;
import com.example.model.Band;
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
public class BandController {

    private final BandService bandService;
    private final SongService songService;

    @GetMapping("/bands")
    public String bands(ModelMap modelMap,
                        @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC)
                        Pageable pageable) {

        Page<Band> bands = bandService.findAll(pageable);

        modelMap.addAttribute("bands", bands);
        modelMap.addAttribute("pageNumbers", bandService.getPageNumbers(bands));
        modelMap.addAttribute("currentSort",
                pageable.getSort().stream()
                        .findFirst()
                        .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                        .orElse("id,desc")
        );

        return "bands";
    }

    @PostMapping("/bands")
    public String addBand(@ModelAttribute Band band, @RequestParam("bandImage") MultipartFile bandImage) {
        bandService.create(band, bandImage);
        return "redirect:/bands";
    }

    @GetMapping("/bands/add")
    public String addBand() {
        return "addBand";
    }

    @GetMapping("/bands/edit")
    public String editBand(@RequestParam("id") Long id, ModelMap modelMap) {
        Band band = bandService.getBandById(id);
        modelMap.addAttribute("band", band);
        return "editBand";
    }

    @PostMapping("/bands/edit")
    public String editBand(@ModelAttribute Band editedBand,
                           @RequestParam("bandImage") MultipartFile bandImage) {
        bandService.update(editedBand, bandImage);
        return "redirect:/bands";
    }

    @GetMapping("/bands/delete")
    public String deleteBand(@RequestParam("id") Long id) {
        bandService.delete(id);
        return "redirect:/bands";
    }

    @GetMapping("/bands/preview")
    public String bandPreviewPage(@RequestParam("id") Long id, ModelMap modelMap) {
        Band band = bandService.getBandByIdForArtists(id);
        List<SongDto> songs = songService.getTop5SongsOfBandByPlayCount(id);
        modelMap.addAttribute("band", band);
        modelMap.addAttribute("songs", songs);
        return "bandPreview";
    }
}