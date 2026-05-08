package com.example.rest.endpoint;

import com.example.dto.SongCommentDto;
import com.example.dto.SongCommentReactionRequest;
import com.example.dto.SongCommentRequest;
import com.example.dto.SongDto;
import com.example.rest.service.security.SpringUser;
import com.example.service.SongCommentReactionService;
import com.example.service.SongCommentService;
import com.example.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/songs")
public class SongEndpoint {
    private final SongService songService;
    private final SongCommentService songCommentService;
    private final SongCommentReactionService songCommentReactionService;

    @GetMapping("/{id}")
    public SongDto getSong(@PathVariable Long id) {
        return songService.getSongDtoById(id);
    }
    @GetMapping("/{albumId}/comments")
    public Page<SongCommentDto> viewComments(@PageableDefault Pageable pageable, @PathVariable Long albumId) {
        return songCommentService.findAllDto(pageable, albumId);
    }

    @GetMapping("/comments/{id}")
    public SongCommentDto viewComment(@PathVariable Long id) {
        return songCommentService.getSongComment(id);
    }

    @PostMapping("/comments")
    public SongCommentDto saveComment(@RequestBody @Valid SongCommentRequest request, @AuthenticationPrincipal SpringUser user) {
        return songCommentService.createSongComment(request, user.getUser());
    }

    @PostMapping("/comments/rate")
    public SongCommentDto saveReaction(@RequestBody @Valid SongCommentReactionRequest request, @AuthenticationPrincipal SpringUser user) {
        songCommentReactionService.saveCommentReaction(request, user.getUser());
        return viewComment(request.getCommentId());
    }
}
