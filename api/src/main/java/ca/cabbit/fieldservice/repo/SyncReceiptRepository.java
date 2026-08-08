package ca.cabbit.fieldservice.repo;
import ca.cabbit.fieldservice.domain.SyncReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface SyncReceiptRepository extends JpaRepository<SyncReceipt, UUID> {}

