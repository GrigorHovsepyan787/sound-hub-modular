package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlbumCommentDto {
    private Long id;
    private String content;
    private Long userId;
    private Long albumId;
    private int rating;
    private LocalDateTime createdAt;
    private boolean deleted;
}
