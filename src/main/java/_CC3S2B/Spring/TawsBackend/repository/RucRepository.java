package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Ruc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RucRepository extends JpaRepository<Ruc, Long> {

    Optional<Ruc> findByUser_UserIdAndRucNumber(Integer userId, String rucNumber);

    Optional<Ruc> findFirstByUser_UserIdOrderByUpdatedAtDesc(Integer userId);

    Optional<Ruc> findFirstByRucNumberOrderByUpdatedAtDesc(String rucNumber);
}
