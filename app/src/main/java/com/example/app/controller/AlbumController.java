package com.example.app.controller;


import com.example.model.Album;
import com.example.service.AlbumService;
import com.example.service.ArtistService;
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
    private final ArtistService artistService;

    @GetMapping("/albums")
    public String albums(ModelMap modelMap,
                         @PageableDefault(sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        modelMap.addAttribute("albums", albumService.findAlbumPage(pageable));
        return "albums";
    }

    @GetMapping("/albums/preview")
    public String preview(ModelMap modelMap, @RequestParam("id") Long id) {
        modelMap.addAttribute("album", albumService.findAlbumById(id));
        return "albumPreview";
    }

    @GetMapping("/albums/add")
    public String addAlbum(ModelMap modelMap,
                           @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(value = "bandName", required = false) String bandName,
                           @RequestParam(value = "artistName", required = false) String artistName) {
        modelMap.addAttribute("bands", bandService.getBandsByName(bandName, pageable));
        modelMap.addAttribute("artists", artistService.getArtistsByName(artistName, pageable));

        return "addAlbum";
    }

    @GetMapping("/albums/update")
    public String updateAlbum(ModelMap modelMap,
                              @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                              @RequestParam(value = "bandName", required = false) String bandName,
                              @RequestParam(value = "artistName", required = false) String artistName,
                              @RequestParam("id")  Long id) {
        modelMap.addAttribute("bands", bandService.getBandsByName(bandName, pageable));
        modelMap.addAttribute("artists", artistService.getArtistsByName(artistName, pageable));
        modelMap.addAttribute("album", albumService.findAlbumById(id));
        return "editAlbum";
    }

    @PostMapping("/albums/add")
    public String addAlbum(@ModelAttribute Album album,
                           @RequestParam("pic") MultipartFile multipartfile,
                           @RequestParam(value = "bandId", required = false) Long bandId,
                           @RequestParam(value = "artistId", required = false) Long artistId) {
        if (artistId == null && bandId == null) {
            return "redirect:/albums";
        }
        albumService.save(album, multipartfile, bandId, artistId);
        return "redirect:/albums";
    }

    @PostMapping("/albums/update")
    public String updateAlbum(@ModelAttribute Album album, @RequestParam("pic") MultipartFile multipartFile) {
        albumService.update(album, multipartFile);
        return "redirect:/albums";
    }
}
