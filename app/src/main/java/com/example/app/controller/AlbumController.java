package com.example.app.controller;


import com.example.model.Album;
import com.example.service.AlbumService;
import com.example.service.BandService;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;
    private final BandService bandService;
    // private final ArtistService artistService;

    @GetMapping("/albums")
    public String albums() {
        return "albums";
    }

    @GetMapping("/albums/add")
    public String addAlbum(ModelMap modelMap,
                           @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam("bandName") String bandName,
                           @RequestParam("artistName") String artistName) {
        modelMap.addAttribute("bands", bandService.getBandsByName(bandName, pageable));
        //modelMap.addAttribute("artists", artistService.getArtistsByName(artistName, pageable));

        return "addAlbum";
    }

    @PostMapping("/albums/add")
    public String addAlbum(@ModelAttribute Album album,
                           @RequestParam("pic") MultipartFile multipartfile) {
        albumService.save(album, multipartfile);
        return "redirect:/albums";
    }
}
