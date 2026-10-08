package _CC3S2B.Spring.TawsBackend.repository;
import _CC3S2B.Spring.TawsBackend.model.RolModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolModuleRepository extends JpaRepository<RolModule, Integer> {
}
