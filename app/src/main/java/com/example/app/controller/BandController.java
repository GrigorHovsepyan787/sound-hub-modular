package com.example.app.controller;

import com.example.model.Band;
import com.example.service.BandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class BandController {

    private final BandService bandService;

    @GetMapping("/bands")
    public String bands(ModelMap modelMap,
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size,
                        @RequestParam("sort") Optional<String> sortParam) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(6);

        String sortValue = sortParam.orElse("id,desc");
        String[] sortParts = sortValue.split(",");

        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortParts.length > 1) {
            direction = Sort.Direction.fromString(sortParts[1]);
        }

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(direction, sortField));

        Page<Band> bands = bandService.findAll(pageable);

        int totalPages = bands.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .toList();
            modelMap.addAttribute("pageNumbers", pageNumbers);
        }

        modelMap.addAttribute("bands", bands);
        modelMap.addAttribute("currentSort", sortValue);

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
        Band band = bandService.getBandById(id);
        modelMap.addAttribute("band", band);
        return "bandPreview";
    }
}
