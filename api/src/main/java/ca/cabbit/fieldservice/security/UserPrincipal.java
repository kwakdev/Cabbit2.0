package ca.cabbit.fieldservice.security;

import ca.cabbit.fieldservice.domain.Role;
import java.util.UUID;

public record UserPrincipal(UUID id, UUID companyId, String email, String displayName, Role role) {}

