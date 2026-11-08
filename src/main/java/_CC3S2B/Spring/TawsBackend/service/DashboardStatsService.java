
package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.DashboardStatsResponse;
import _CC3S2B.Spring.TawsBackend.dto.DashboardStatsResponse.ChartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardStatsResponse getStatistics() {

        List<ChartItem> destinations = jdbcTemplate.query("""
                SELECT destination_region AS label,
                       COUNT(*) AS value
                FROM taws.quotation
                WHERE destination_region IS NOT NULL
                GROUP BY destination_region
                ORDER BY value DESC, label
                LIMIT 10
                """,
                (rs, rowNum) -> new ChartItem(
                        rs.getString("label"),
                        rs.getLong("value")
                )
        );

        List<ChartItem> statuses = jdbcTemplate.query("""
                SELECT status AS label,
                       COUNT(*) AS value
                FROM taws.quotation
                GROUP BY status
                ORDER BY value DESC
                """,
                (rs, rowNum) -> new ChartItem(
                        rs.getString("label"),
                        rs.getLong("value")
                )
        );

        List<ChartItem> registrations = jdbcTemplate.query("""
                SELECT TO_CHAR(
                           DATE_TRUNC('month', p.created_at),
                           'YYYY-MM'
                       ) AS label,
                       COUNT(*) AS value
                FROM taws.app_user u
                INNER JOIN taws.person p
                    ON p.person_id = u.person_id
                WHERE p.created_at IS NOT NULL
                GROUP BY DATE_TRUNC('month', p.created_at)
                ORDER BY DATE_TRUNC('month', p.created_at)
                """,
                (rs, rowNum) -> new ChartItem(
                        rs.getString("label"),
                        rs.getLong("value")
                )
        );

        return new DashboardStatsResponse(
                destinations,
                statuses,
                registrations
        );
    }
}
