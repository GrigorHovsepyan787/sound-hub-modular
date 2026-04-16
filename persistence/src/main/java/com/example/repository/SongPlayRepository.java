package com.example.repository;

import com.example.model.SongPlay;
import com.example.projection.AlbumPopularity;
import com.example.projection.ArtistPopularity;
import com.example.projection.BandPopularity;
import com.example.projection.SongPopularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Query(
            value = """
                    SELECT
                        s.band AS band,
                        COUNT(sp.id) AS totalPlays
                    FROM SongPlay sp
                    JOIN sp.song s
                    WHERE
                        s.band IS NOT NULL
                        AND sp.playedAt >= :start
                        AND sp.playedAt < :end
                    GROUP BY s.band.id
                    ORDER BY COUNT(sp.id) DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT s.band.id)
                    FROM SongPlay sp
                    JOIN sp.song s
                    WHERE
                        s.band IS NOT NULL
                        AND sp.playedAt >= :start
                        AND sp.playedAt < :end
                    """
    )
    Page<BandPopularity> findTopBandsForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        s.album AS album,
                        COUNT(sp.id) AS totalPlays
                    FROM SongPlay sp
                    JOIN sp.song s
                    WHERE
                        s.album IS NOT NULL
                        AND sp.playedAt >= :start
                        AND sp.playedAt < :end
                    GROUP BY s.album.id
                    ORDER BY COUNT(sp.id) DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT s.album.id)
                    FROM SongPlay sp
                    JOIN sp.song s
                    WHERE
                        s.album IS NOT NULL
                        AND sp.playedAt >= :start
                        AND sp.playedAt < :end
                    """
    )
    Page<AlbumPopularity> findTopAlbumsForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        sp.song AS song,
                        COUNT(sp.id) AS totalPlays
                    FROM SongPlay sp
                    WHERE
                        sp.playedAt >= :start
                        AND sp.playedAt < :end
                    GROUP BY sp.song.id
                    ORDER BY COUNT(sp.id) DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT sp.song.id)
                    FROM SongPlay sp
                    WHERE
                        sp.playedAt >= :start
                        AND sp.playedAt < :end
                    """
    )
    Page<SongPopularity> findTopSongsForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM SongPlay sp WHERE sp.song.id = :songId")
    void deleteBySongId(@Param("songId") Long songId);

    long countByPlayedAtBetween(LocalDateTime start, LocalDateTime end);
}
