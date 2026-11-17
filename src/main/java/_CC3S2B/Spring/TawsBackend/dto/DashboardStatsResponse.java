package _CC3S2B.Spring.TawsBackend.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record DashboardStatsResponse(
        List<ChartItem> destinations,
        List<ChartItem> statuses,
        List<ChartItem> registrations
) {
    public record ChartItem(String label, Long value) {}
}
