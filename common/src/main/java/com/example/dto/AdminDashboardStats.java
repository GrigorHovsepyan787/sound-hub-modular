package com.example.dto;

public record AdminDashboardStats(
        long totalListening,
        long totalUsers,
        long totalArtists,
        double listeningGrowthPercent,
        double usersGrowthPercent,
        double artistsGrowthPercent
) {}