package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.QuoteActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface QuoteActivityRepository extends JpaRepository<QuoteActivity, Integer> {
 List<QuoteActivity> findByDestinationId(Integer destinationId);
}
