package _CC3S2B.Spring.TawsBackend.dto;

import java.util.List;

public record DashboardStatsResponse(
        List<ChartItem> destinations,
        List<ChartItem> statuses,
        List<ChartItem> registrations
) {
    public record ChartItem(String label, Long value) {}
}
