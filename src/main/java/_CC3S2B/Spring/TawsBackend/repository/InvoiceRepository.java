package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByTransaction_TransactionId(Long transactionId);

    @Query(value = "SELECT invoice_number FROM taws.invoice ORDER BY transaction_id DESC LIMIT 1", nativeQuery = true)
    String findLastInvoiceNumber();
}
