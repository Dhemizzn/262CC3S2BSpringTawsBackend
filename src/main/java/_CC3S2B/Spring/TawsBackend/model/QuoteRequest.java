package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotation", schema = "taws")
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_id")
    private Long numId;

    @Column(name = "quotation_id", insertable = false, updatable = false)
    private String quotationId;

    @Column(name = "status", length = 10, nullable = false)
    @Builder.Default
    private String status = "pending";

    @Column(name = "people_quantity", nullable = false)
    private Integer peopleQuantity;

    @Column(name = "departure_region", length = 50, nullable = false)
    private String departureRegion;

    @Column(name = "destination_region", length = 50, nullable = false)
    private String destinationRegion;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "return_time")
    private LocalTime returnTime;

    @Column(name = "total")
    private BigDecimal total;
}