package com.example.repository;

import com.example.projection.ArtistPopularity;
import com.example.model.SongPlay;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SongPlayRepository extends JpaRepository<SongPlay, Long> {
    @Query(
            value = """
                    SELECT
                        s.artist AS artist,
                        COUNT(sp.id) AS totalPlays
                    FROM SongPlay sp
                    JOIN sp.song s
                    WHERE
                        s.artist IS NOT NULL
                        AND sp.playedAt >= :start
                        AND sp.playedAt < :end
                    GROUP BY s.artist.id
                    ORDER BY COUNT(sp.id) DESC
                    """,
            countQuery = """
        SELECT COUNT(DISTINCT s.artist.id)
        FROM SongPlay sp
        JOIN sp.song s
        WHERE
            s.artist IS NOT NULL
            AND sp.playedAt >= :start
            AND sp.playedAt < :end
        """
    )
    Page<ArtistPopularity> findTopArtistsForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
