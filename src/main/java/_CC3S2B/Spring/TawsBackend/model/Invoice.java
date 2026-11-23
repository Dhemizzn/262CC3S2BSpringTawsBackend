package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "invoice", schema = "taws")
public class Invoice {

    @Id
    @Column(name = "transaction_id")
    private Long transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "invoice_number", length = 20, nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "ruc", length = 20, nullable = false)
    private String ruc;

    @Column(name = "business_name", length = 100, nullable = false)
    private String businessName;

    @Column(name = "fiscal_address", length = 255, nullable = false)
    private String fiscalAddress;
}
