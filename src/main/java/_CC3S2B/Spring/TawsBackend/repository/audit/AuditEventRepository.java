package _CC3S2B.Spring.TawsBackend.repository.audit;

import _CC3S2B.Spring.TawsBackend.model.audit.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
