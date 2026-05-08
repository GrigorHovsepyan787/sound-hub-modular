package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongCommentDto {
    private Long id;
    private String content;
    private Long userId;
    private Long songId;
    private int rating;
    private LocalDateTime createdAt;
    private boolean deleted;
}
