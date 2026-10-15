package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Integer> {
    @Query(value = """
        SELECT quotation_id
        FROM taws.quotation
        WHERE num_id = :numId
        """, nativeQuery = true)
    String findGeneratedQuotationId(
            @Param("numId") Integer numId
    );
}
