package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Airplane;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AirplaneRepository extends JpaRepository<Airplane, Integer> {
 List<Airplane> findByDestinationId(Integer destinationId);
}
