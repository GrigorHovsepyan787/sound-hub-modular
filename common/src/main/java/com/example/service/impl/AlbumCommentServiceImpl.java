package com.example.service.impl;

import com.example.dto.AlbumCommentRequest;
import com.example.mapper.AlbumCommentRequestMapper;
import com.example.model.Album;
import com.example.model.AlbumComment;
import com.example.model.User;
import com.example.repository.AlbumCommentRepository;
import com.example.repository.AlbumRepository;
import com.example.service.AlbumCommentService;
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

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class AlbumCommentServiceImpl implements AlbumCommentService {
    private final AlbumCommentRepository albumCommentRepository;
    private final AlbumRepository albumRepository;
    private final AlbumCommentRequestMapper albumCommentRequestMapper;

    @Override
    public Page<AlbumComment> findAll(Pageable pageable) {
        return albumCommentRepository.findAll(pageable);
    }

    @Override
    public String save(AlbumCommentRequest request, User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.albumCommentRequest",
                    bindingResult
            );

            redirectAttributes.addFlashAttribute(
                    "albumCommentRequest",
                    request
            );

            return "redirect:/albums/preview?id=" + request.getAlbumId();
        }
        log.info("Attempting to create album comment for albumId={} by user={}",
                request.getAlbumId(),
                user.getUsername());
        Album album = albumRepository.findById(request.getAlbumId()).orElseThrow(() -> {
            log.warn("Album not found. albumId={}", request.getAlbumId());
            return new EntityNotFoundException("Album not found");
        });
        AlbumComment albumComment = albumCommentRequestMapper.toEntity(request);
        albumComment.setUser(user);
        albumComment.setAlbum(album);
        albumCommentRepository.save(albumComment);
        log.info("Album comment successfully created. albumId={}, user={}",
                album.getId(),
                user.getUsername());
        return "redirect:/albums/preview?id=" + request.getAlbumId();
    }

    @Override
    public void setDeleted(Long id, boolean deleted) {
        AlbumComment albumComment = albumCommentRepository.findById(id).orElseThrow(() -> {
            log.warn("Album comment not found. id={}", id);
            return new EntityNotFoundException("Album comment not found");
        });
        if (albumComment.isDeleted() && deleted) {
            log.warn("Album comment already marked as deleted. commentId={}", id);
            return;
        }
        if (!albumComment.isDeleted() && !deleted) {
            log.warn("Album comment wasn't marked as deleted. commentId={}", id);
            return;
        }
        albumComment.setDeleted(deleted);
        albumCommentRepository.save(albumComment);
        if (deleted) {
            log.info("Album comment was soft deleted successfully. commentId={}", id);
        } else {
            log.info("Album comment was restored successfully. commentId={}", id);
        }
    }

    @Override
    public void permanentDelete(Long id) {
        log.info("Attempting to permanently delete album comment. commentId={}", id);

        if (!albumCommentRepository.existsById(id)) {
            log.warn("Album comment not found for permanent delete. commentId={}", id);
            throw new EntityNotFoundException("Album comment not found");
        }

        albumCommentRepository.deleteById(id);
        log.info("Album comment permanently deleted successfully. commentId={}", id);
    }

    @Override
    public void isAlbumCommentRequestPresent(ModelMap modelMap) {
        if (!modelMap.containsAttribute("albumCommentRequest")) {
            modelMap.addAttribute("albumCommentRequest", new AlbumCommentRequest());
        }
    }
}
