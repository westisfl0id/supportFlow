package com.supportflow.statistics.dto;

public record StatisticsResponse(
        String role,
        long totalTickets,
        long openTickets,
        long resolvedTickets,
        long closedTickets,
        long slaBreachedTickets,
        Double averageFirstResponseMinutes,
        Double averageResolutionMinutes
) {
}
