package com.example.app.controller;

import com.example.app.service.security.SpringUser;
import com.example.dto.AlbumCommentReactionRequest;
import com.example.dto.AlbumCommentRequest;
import com.example.model.Album;
import com.example.service.AlbumCommentReactionService;
import com.example.service.AlbumCommentService;
import com.example.service.AlbumService;
import com.example.service.ArtistService;
import com.example.service.BandService;
import com.example.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;
    private final BandService bandService;
    private final SongService songService;
    private final ArtistService artistService;
    private final AlbumCommentService albumCommentService;
    private final AlbumCommentReactionService albumCommentReactionService;

    @GetMapping("/albums")
    public String albums(ModelMap modelMap,
                         @PageableDefault(sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        modelMap.addAttribute("albums", albumService.findAlbumPage(pageable));
        return "albums";
    }

    @GetMapping("/albums/preview")
    public String preview(ModelMap modelMap, @RequestParam("id") Long id,
                          @PageableDefault(sort = "rating", direction = Sort.Direction.DESC, size = 5) Pageable pageable) {
        modelMap.addAttribute("album", albumService.findAlbumById(id));
        modelMap.addAttribute("songs", songService.getSongsByAlbumId(id));
        modelMap.addAttribute("comments", albumCommentService.findAll(pageable, id));
        albumCommentService.isAlbumCommentRequestPresent(modelMap);
        return "albumPreview";
    }

    @GetMapping("/albums/add")
    public String addAlbum(ModelMap modelMap,
                           @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(value = "bandName", required = false) String bandName,
                           @RequestParam(value = "artistName", required = false) String artistName) {
        modelMap.addAttribute("bands", bandService.getBandsByName(bandName, pageable));
        modelMap.addAttribute("artists", artistService.getArtistsByName(artistName, pageable));
        modelMap.addAttribute("now", LocalDateTime.now());
        return "addAlbum";
    }

    @GetMapping("/albums/delete")
    public String deleteAlbum(@RequestParam("id") Long id) {
        albumService.delete(id);
        return "redirect:/albums";
    }

    @GetMapping("/albums/update")
    public String updateAlbum(ModelMap modelMap,
                              @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                              @RequestParam(value = "bandName", required = false) String bandName,
                              @RequestParam(value = "artistName", required = false) String artistName,
                              @RequestParam("id") Long id) {
        modelMap.addAttribute("bands", bandService.getBandsByName(bandName, pageable));
        modelMap.addAttribute("artists", artistService.getArtistsByName(artistName, pageable));
        modelMap.addAttribute("now", LocalDateTime.now());
        modelMap.addAttribute("album", albumService.findAlbumById(id));
        return "editAlbum";
    }

    @GetMapping("/albums/comment/delete")
    public String deleteComment(@RequestParam("commentId") Long commentId,
                                @RequestParam("albumId") Long albumId) {
        albumCommentService.permanentDelete(commentId);
        return "redirect:/albums/preview?id=" + albumId;
    }

    @GetMapping("/albums/comment/softDelete")
    public String softDeleteComment(@RequestParam("commentId") Long commentId,
                                    @RequestParam("albumId") Long albumId) {
        albumCommentService.setDeleted(commentId, true);
        return "redirect:/albums/preview?id=" + albumId;
    }

    @GetMapping("/albums/comment/restore")
    public String restoreComment(@RequestParam("commentId") Long commentId,
                                 @RequestParam("albumId") Long albumId) {
        albumCommentService.setDeleted(commentId, false);
        return "redirect:/albums/preview?id=" + albumId;
    }

    @PostMapping("/albums/comment/add")
    public String addComment(@Valid @ModelAttribute("albumCommentRequest") AlbumCommentRequest albumCommentRequest,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal SpringUser currentUser,
                             RedirectAttributes redirectAttributes) {
        return albumCommentService.save(albumCommentRequest, currentUser.getUser(), bindingResult, redirectAttributes);
    }

    @PostMapping("/albums/add")
    public String addAlbum(@ModelAttribute Album album,
                           @RequestParam("pic") MultipartFile multipartfile,
                           @RequestParam(value = "bandId", required = false) Long bandId,
                           @RequestParam(value = "artistId", required = false) Long artistId) {
        albumService.save(album, multipartfile, bandId, artistId);
        return "redirect:/albums";
    }

    @PostMapping("/albums/update")
    public String updateAlbum(@ModelAttribute Album album,
                              @RequestParam("pic") MultipartFile multipartFile,
                              @RequestParam(value = "bandId", required = false) Long bandId,
                              @RequestParam(value = "artistId", required = false) Long artistId) {
        albumService.update(album, multipartFile, bandId, artistId);
        return "redirect:/albums";
    }

    @PostMapping("/albums/comment/rate")
    public String rateComment(@Valid @ModelAttribute AlbumCommentReactionRequest request,
                              @AuthenticationPrincipal SpringUser currentUser,
                              @RequestParam("albumId") Long id) {
        albumCommentReactionService.saveCommentReaction(request, currentUser.getUser());
        return "redirect:/albums/preview?id=" + id;
    }
}
