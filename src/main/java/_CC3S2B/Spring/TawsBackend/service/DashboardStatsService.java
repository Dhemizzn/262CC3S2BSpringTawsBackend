package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.DashboardStatsResponse;
import _CC3S2B.Spring.TawsBackend.dto.DashboardStatsResponse.ChartItem;
import _CC3S2B.Spring.TawsBackend.repository.DashboardStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final DashboardStatsRepository dashboardStatsRepository;

    public DashboardStatsResponse getStatistics() {

        List<ChartItem> destinations = dashboardStatsRepository.findTopDestinations().stream()
                .map(row -> new ChartItem(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();

        List<ChartItem> statuses = dashboardStatsRepository.findQuotationStatuses().stream()
                .map(row -> new ChartItem(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();

        List<ChartItem> registrations = dashboardStatsRepository.findMonthlyRegistrations().stream()
                .map(row -> new ChartItem(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();

        return DashboardStatsResponse.builder()
                .destinations(destinations)
                .statuses(statuses)
                .registrations(registrations)
                .build();
    }
}