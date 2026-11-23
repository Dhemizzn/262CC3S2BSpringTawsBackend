package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByQuotationId(String quotationId);
}
