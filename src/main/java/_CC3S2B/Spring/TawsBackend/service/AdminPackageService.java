package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.AdminPackageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPackageService {

    private final JdbcTemplate jdbcTemplate;

    public List<AdminPackageDTO> findPackages(LocalDate startDate, LocalDate endDate,
                                              BigDecimal minAmount, BigDecimal maxAmount,
                                              Integer userId, String quotationId,
                                              String status) {

        StringBuilder sql = new StringBuilder("""
                SELECT q.quotation_id, q.status, q.people_quantity,
                       q.departure_region, q.destination_region,
                       q.departure_date, q.return_date, q.total,
                       q.app_user_id, q.mp_order_id,
                       q.itinerary::text AS itinerary,
                       CONCAT(p.first_name, ' ', p.paternal_surname, ' ', p.maternal_surname) AS client_name,
                       p.dni AS client_dni,
                       p.email AS client_email
                FROM taws.quotation q
                INNER JOIN taws.app_user u ON u.user_id = q.app_user_id
                INNER JOIN taws.person p ON p.person_id = u.person_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND q.departure_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND q.return_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (minAmount != null) {
            sql.append(" AND q.total >= ?");
            params.add(minAmount);
        }
        if (maxAmount != null) {
            sql.append(" AND q.total <= ?");
            params.add(maxAmount);
        }
        if (userId != null) {
            sql.append(" AND q.app_user_id = ?");
            params.add(userId);
        }
        if (quotationId != null && !quotationId.isBlank()) {
            sql.append(" AND q.quotation_id = ?");
            params.add(quotationId.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND q.status = ?");
            params.add(status.trim().toLowerCase());
        }

        sql.append(" ORDER BY q.num_id DESC LIMIT 500");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) ->
                AdminPackageDTO.builder()
                        .quotationId(rs.getString("quotation_id"))
                        .status(rs.getString("status"))
                        .peopleQuantity(rs.getInt("people_quantity"))
                        .departureRegion(rs.getString("departure_region"))
                        .destinationRegion(rs.getString("destination_region"))
                        .departureDate(rs.getDate("departure_date").toLocalDate())
                        .returnDate(rs.getDate("return_date").toLocalDate())
                        .total(rs.getBigDecimal("total"))
                        .userId(rs.getInt("app_user_id"))
                        .mpOrderId(rs.getString("mp_order_id"))
                        .itinerary(rs.getString("itinerary"))
                        .clientName(rs.getString("client_name"))
                        .clientDni(rs.getString("client_dni"))
                        .clientEmail(rs.getString("client_email"))
                        .build()
        );
    }
}
