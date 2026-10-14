package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DestinationRepository extends JpaRepository<Destination, Integer> {
 List<Destination> findAllByOrderByNameAsc();
}
