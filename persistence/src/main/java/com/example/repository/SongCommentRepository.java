package com.example.repository;

import com.example.model.SongComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongCommentRepository extends JpaRepository<SongComment, Long> {
    Page<SongComment> findBySongId(Long songId, Pageable pageable);

    List<SongComment> findBySongId(Long songId);
}
