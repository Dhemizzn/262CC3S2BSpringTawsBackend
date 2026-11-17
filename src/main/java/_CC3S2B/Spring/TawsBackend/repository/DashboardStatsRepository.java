package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardStatsRepository extends JpaRepository<Quotation, Integer> {

    @Query(value = """
            SELECT destination_region AS label,
                   COUNT(*) AS value
            FROM taws.quotation
            WHERE destination_region IS NOT NULL
            GROUP BY destination_region
            ORDER BY value DESC, label
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTopDestinations();

    @Query(value = """
            SELECT status AS label,
                   COUNT(*) AS value
            FROM taws.quotation
            GROUP BY status
            ORDER BY value DESC
            """, nativeQuery = true)
    List<Object[]> findQuotationStatuses();

    @Query(value = """
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
            """, nativeQuery = true)
    List<Object[]> findMonthlyRegistrations();
}