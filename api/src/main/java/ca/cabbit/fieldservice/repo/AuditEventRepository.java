package ca.cabbit.fieldservice.repo;
import ca.cabbit.fieldservice.domain.AuditEvent;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    Page<AuditEvent> findByCompanyIdOrderByOccurredAtDesc(UUID companyId, Pageable pageable);
}

