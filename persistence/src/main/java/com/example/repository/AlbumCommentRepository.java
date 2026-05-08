package com.example.repository;

import com.example.model.AlbumComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumCommentRepository extends JpaRepository<AlbumComment, Long> {
    Page<AlbumComment> findByAlbumId(Long albumId, Pageable pageable);

    List<AlbumComment> findByAlbumId(Long albumId);
}
