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
@Table(name = "receipt", schema = "taws")
public class Receipt {

    @Id
    @Column(name = "transaction_id")
    private Long transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "receipt_number", length = 20, nullable = false, unique = true)
    private String receiptNumber;
}
