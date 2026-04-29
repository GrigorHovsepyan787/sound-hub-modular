package com.example.service;

import com.example.dto.SongCommentRequest;
import com.example.model.SongComment;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface SongCommentService {
    Page<SongComment> findAll(Pageable pageable);

    String save(SongCommentRequest request, User user, BindingResult bindingResult, RedirectAttributes redirectAttributes);

    void setDeleted(Long id, boolean deleted);

    void permanentDelete(Long id);

    void isSongCommentRequestPresent(ModelMap modelMap);
}
