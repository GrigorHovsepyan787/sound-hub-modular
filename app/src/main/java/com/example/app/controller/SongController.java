package com.example.app.controller;

import com.example.app.service.security.SpringUser;
import com.example.dto.SongCommentReactionRequest;
import com.example.dto.SongCommentRequest;
import com.example.dto.SongDto;
import com.example.model.Genre;
import com.example.service.AlbumService;
import com.example.service.ArtistService;
import com.example.service.BandService;
import com.example.service.SongCommentReactionService;
import com.example.service.SongCommentService;
import com.example.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final ArtistService artistService;
    private final BandService bandService;
    private final AlbumService albumService;
    private final SongCommentService songCommentService;
    private final SongCommentReactionService songCommentReactionService;

    @GetMapping("/songs")
    public String songs(ModelMap modelMap,
                        @RequestParam(required = false) Genre genre,
                        @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC)
                        Pageable pageable) {

        Page<SongDto> songs = songService.findSongsByGenre(genre, pageable);

        modelMap.addAttribute("songs", songs);
        modelMap.addAttribute("genres", Genre.values());
        modelMap.addAttribute("selectedGenre", genre);
        modelMap.addAttribute("pageNumbers", songService.getPageNumbers(songs));
        modelMap.addAttribute("currentSort",
                pageable.getSort().stream()
                        .findFirst()
                        .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                        .orElse("id,desc")
        );

        return "songs";
    }

    @PostMapping("/songs")
    public String addSong(@ModelAttribute SongDto songDto,
                          @RequestParam("songUrl") MultipartFile multipartFile,
                          RedirectAttributes redirectAttributes) {
        songService.save(songDto, multipartFile);
        redirectAttributes.addFlashAttribute("success", "Song added successfully!");
        return "redirect:/songs";
    }

    @GetMapping("/songs/add")
    public String addSong(ModelMap modelMap) {
        modelMap.addAttribute("genres", Genre.values());
        modelMap.addAttribute("artists", artistService.findAll());
        modelMap.addAttribute("bands", bandService.findAll());
        modelMap.addAttribute("albums", albumService.findAll());
        return "addSong";
    }

    @GetMapping("/songs/delete")
    public String deleteSong(@RequestParam("id") Long id) {
        songService.delete(id);
        return "redirect:/songs";
    }

    @GetMapping("/songs/play")
    public String registerPlay(@RequestParam("id") Long id) {
        songService.registerPlay(id);
        return "redirect:/songs";
    }

    @GetMapping("/songs/preview")
    public String previewSong(ModelMap modelMap,
                              @RequestParam("id") Long id,
                              @PageableDefault(sort = "rating", direction = Sort.Direction.DESC, size = 5) Pageable pageable) {
        modelMap.addAttribute("song", songService.getSongById(id));
        modelMap.addAttribute("comments", songCommentService.findAll(pageable, id));
        modelMap.addAttribute("albumSongs", songService.getSongsByAlbumId(songService.getSongById(id).getAlbumId()));
        songCommentService.isSongCommentRequestPresent(modelMap);
        return "songPreview";
    }

    @GetMapping("/songs/comment/delete")
    public String deleteComment(@RequestParam("commentId") Long commentId,
                                @RequestParam("songId") Long songId) {
        songCommentService.permanentDelete(commentId);
        return "redirect:/songs/preview?id=" + songId;
    }

    @GetMapping("/songs/comment/softDelete")
    public String softDeleteComment(@RequestParam("commentId") Long commentId,
                                    @RequestParam("songId") Long songId) {
        songCommentService.setDeleted(commentId, true);
        return "redirect:/songs/preview?id=" + songId;
    }

    @GetMapping("/songs/comment/restore")
    public String restoreComment(@RequestParam("commentId") Long commentId,
                                    @RequestParam("songId") Long songId) {
        songCommentService.setDeleted(commentId, false);
        return "redirect:/songs/preview?id=" + songId;
    }

    @PostMapping("/songs/comment/rate")
    public String rateComment(@Valid @ModelAttribute SongCommentReactionRequest request,
                              @AuthenticationPrincipal SpringUser currentUser,
                              @RequestParam("songId") Long id) {
        songCommentReactionService.saveCommentReaction(request, currentUser.getUser());
        return "redirect:/songs/preview?id=" + id;
    }

    @PostMapping("/songs/comment/add")
    public String addComment(@Valid @ModelAttribute("albumCommentRequest") SongCommentRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal SpringUser currentUser,
                             RedirectAttributes redirectAttributes) {
        return songCommentService.save(request, currentUser.getUser(), bindingResult, redirectAttributes);
    }

    @GetMapping("/songs/search")
    @ResponseBody
    public List<SongDto> search(@RequestParam(defaultValue = "") String q,
                                @RequestParam(defaultValue = "10") int limit) {
        return songService.searchSongs(q, limit);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("performer_type", "has_album", "songUrl");
    }
}