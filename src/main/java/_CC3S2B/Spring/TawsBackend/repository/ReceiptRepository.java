package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByTransaction_TransactionId(Long transactionId);

    @Query(value = "SELECT receipt_number FROM taws.receipt ORDER BY transaction_id DESC LIMIT 1", nativeQuery = true)
    String findLastReceiptNumber();
}
