package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Entity
@Data
@Table(name = "quotation", schema = "taws")
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_id")
    private Integer numId;

    @Column(name = "quotation_id", insertable = false, updatable = false, length = 20)
    private String quotationId;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "people_quantity", nullable = false)
    private Integer peopleQuantity;

    @Column(name = "departure_region", nullable = false, length = 50)
    private String departureRegion;

    @Column(name = "destination_region", nullable = false, length = 50)
    private String destinationRegion;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "return_time", nullable = false)
    private LocalTime returnTime;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "app_user_id")
    private Integer appUserId;

    @Column(name = "itinerary", columnDefinition = "jsonb")
    @ColumnTransformer(read = "itinerary::text", write = "CAST(? AS jsonb)")
    private String itinerary;
}
