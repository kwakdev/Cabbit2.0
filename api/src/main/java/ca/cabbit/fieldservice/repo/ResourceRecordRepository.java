package ca.cabbit.fieldservice.repo;
import ca.cabbit.fieldservice.domain.ResourceRecord;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;
public interface ResourceRecordRepository extends JpaRepository<ResourceRecord, UUID> {
    Page<ResourceRecord> findByCompanyIdAndResourceTypeAndDeletedAtIsNull(UUID companyId, String type, Pageable pageable);
    Optional<ResourceRecord> findByIdAndCompanyIdAndResourceTypeAndDeletedAtIsNull(UUID id, UUID companyId, String type);
    List<ResourceRecord> findByCompanyIdAndUpdatedAtAfterOrderByUpdatedAt(UUID companyId, Instant since);
}

