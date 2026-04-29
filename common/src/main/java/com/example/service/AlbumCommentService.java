package com.example.service;

import com.example.dto.AlbumCommentRequest;
import com.example.model.AlbumComment;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface AlbumCommentService {
    Page<AlbumComment> findAll(Pageable pageable);

    String save(AlbumCommentRequest request, User user, BindingResult bindingResult, RedirectAttributes redirectAttributes);

    void setDeleted(Long id, boolean deleted);

    void permanentDelete(Long id);

    void isAlbumCommentRequestPresent(ModelMap modelMap);
}
