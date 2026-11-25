package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(
    name = "ruc",
    schema = "taws",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "ruc_number"})
    }
)
public class Ruc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ruc_id")
    private Long rucId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ruc_number", length = 20, nullable = false)
    private String rucNumber;

    @Column(name = "fiscal_address", length = 255)
    private String fiscalAddress;

    @Column(name = "business_name", length = 100)
    private String businessName;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Ruc(Ruc source) {
        if (source != null) {
            this.rucId = source.rucId;
            this.user = source.user;
            this.rucNumber = source.rucNumber;
            this.fiscalAddress = source.fiscalAddress;
            this.businessName = source.businessName;
            this.updatedAt = source.updatedAt;
        }
    }
}
