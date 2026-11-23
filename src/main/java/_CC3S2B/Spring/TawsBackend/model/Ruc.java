package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "ruc", schema = "taws")
public class Ruc {

    @Id
    @Column(name = "ruc_number", length = 20)
    private String rucNumber;

    @Column(name = "fiscal_address", length = 255)
    private String fiscalAddress;

    @Column(name = "business_name", length = 100)
    private String businessName;
}
