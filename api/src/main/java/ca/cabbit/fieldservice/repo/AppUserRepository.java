package ca.cabbit.fieldservice.repo;
import ca.cabbit.fieldservice.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    List<AppUser> findByCompanyIdOrderByDisplayName(UUID companyId);
}

