package ca.cabbit.fieldservice.admin;

import ca.cabbit.fieldservice.api.ApiException;
import ca.cabbit.fieldservice.domain.*;
import ca.cabbit.fieldservice.repo.*;
import ca.cabbit.fieldservice.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/admin/users") @PreAuthorize("hasAnyRole('ADMINISTRATOR','MANAGER')")
public class AdminController {
    private final AppUserRepository users; private final RefreshTokenRepository tokens; private final PasswordEncoder passwords;
    public AdminController(AppUserRepository users,RefreshTokenRepository tokens,PasswordEncoder passwords){this.users=users;this.tokens=tokens;this.passwords=passwords;}
    @GetMapping public List<UserView> list(@AuthenticationPrincipal UserPrincipal u){return users.findByCompanyIdOrderByDisplayName(u.companyId()).stream().map(UserView::new).toList();}
    @PostMapping @Transactional public UserView create(@Valid @RequestBody CreateUser r,@AuthenticationPrincipal UserPrincipal u){if(users.findByEmailIgnoreCase(r.email()).isPresent())throw ApiException.conflict("Email already exists");return new UserView(users.save(new AppUser(u.companyId(),r.email(),passwords.encode(r.password()),r.displayName(),r.role(),true)));}
    @PatchMapping("/{id}/active") @Transactional public UserView active(@PathVariable UUID id,@RequestBody ActiveRequest r,@AuthenticationPrincipal UserPrincipal u){AppUser user=users.findById(id).filter(x->x.companyId.equals(u.companyId())).orElseThrow(()->ApiException.notFound("User not found"));user.active=r.active();if(!user.active)tokens.revokeAll(user.id,java.time.Instant.now());return new UserView(users.save(user));}
    public record CreateUser(@Email @NotBlank String email,@Size(min=12,max=200) String password,@NotBlank String displayName,@NotNull Role role){}
    public record ActiveRequest(boolean active){}
    public record UserView(UUID id,String email,String displayName,Role role,boolean active,long version){UserView(AppUser u){this(u.id,u.email,u.displayName,u.role,u.active,u.version);}}
}
