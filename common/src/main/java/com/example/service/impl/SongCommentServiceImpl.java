package com.example.service.impl;

import com.example.dto.SongCommentRequest;
import com.example.mapper.SongCommentRequestMapper;
import com.example.model.Song;
import com.example.model.SongComment;
import com.example.model.User;
import com.example.repository.SongCommentRepository;
import com.example.repository.SongRepository;
import com.example.service.SongCommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class SongCommentServiceImpl implements SongCommentService {
    private final SongCommentRepository songCommentRepository;
    private final SongRepository songRepository;
    private final SongCommentRequestMapper songCommentRequestMapper;

    @Override
    public Page<SongComment> findAll(Pageable pageable, Long songId) {
        return songCommentRepository.findBySongId(songId, pageable);
    }

    @Override
    @Transactional
    public String save(SongCommentRequest request, User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.songCommentRequest",
                    bindingResult
            );

            redirectAttributes.addFlashAttribute(
                    "songCommentRequest",
                    request
            );

            return "redirect:/songs/preview?id=" + request.getSongId();
        }
        log.info("Attempting to create song comment for songId={} by user={}",
                request.getSongId(),
                user.getUsername());
        Song song = songRepository.findById(request.getSongId()).orElseThrow(() -> {
            log.warn("Song not found. songId={}", request.getSongId());
            return new EntityNotFoundException("Song not found");
        });
        SongComment songComment = songCommentRequestMapper.toEntity(request);
        songComment.setUser(user);
        songComment.setSong(song);
        songCommentRepository.save(songComment);
        log.info("Song comment successfully created. songId={}, user={}",
                song.getId(),
                user.getUsername());
        return "redirect:/songs/preview?id=" + request.getSongId();
    }

    @Override
    public void setDeleted(Long id, boolean deleted) {
        SongComment songComment = songCommentRepository.findById(id).orElseThrow(() -> {
            log.warn("Song comment not found. id={}", id);
            return new EntityNotFoundException("Song comment not found");
        });
        if (songComment.isDeleted() && deleted) {
            log.warn("Song comment already marked as deleted. commentId={}", id);
            return;
        }
        if (!songComment.isDeleted() && !deleted) {
            log.warn("Song comment wasn't marked as deleted. commentId={}", id);
            return;
        }
        songComment.setDeleted(deleted);
        songCommentRepository.save(songComment);
        if (deleted) {
            log.info("Song comment was soft deleted successfully. commentId={}", id);
        } else {
            log.info("Song comment was restored successfully. commentId={}", id);
        }
    }

    @Override
    public void permanentDelete(Long id) {
        log.info("Attempting to permanently delete song comment. commentId={}", id);

        if (!songCommentRepository.existsById(id)) {
            log.warn("Song comment not found for permanent delete. commentId={}", id);
            throw new EntityNotFoundException("Song comment not found");
        }

        songCommentRepository.deleteById(id);
        log.info("song comment permanently deleted successfully. commentId={}", id);
    }

    @Override
    public void isSongCommentRequestPresent(ModelMap modelMap) {
        if (!modelMap.containsAttribute("songCommentRequest")) {
            modelMap.addAttribute("songCommentRequest", new SongCommentRequest());
        }
    }
}
