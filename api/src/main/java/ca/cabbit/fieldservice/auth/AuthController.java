package ca.cabbit.fieldservice.auth;

import ca.cabbit.fieldservice.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth; public AuthController(AuthService auth){this.auth=auth;}
    @PostMapping("/login") public AuthService.AuthResult login(@Valid @RequestBody LoginRequest r){return auth.login(r.email(),r.password(),r.deviceId());}
    @PostMapping("/refresh") public AuthService.AuthResult refresh(@Valid @RequestBody RefreshRequest r){return auth.refresh(r.refreshToken(),r.deviceId());}
    @PostMapping("/logout") public void logout(@Valid @RequestBody LogoutRequest r){auth.logout(r.refreshToken());}
    @PostMapping("/revoke-all") public void revokeAll(@AuthenticationPrincipal UserPrincipal u){auth.revokeAll(u.id());}
    @PostMapping("/change-password") public void changePassword(@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody ChangePasswordRequest r){auth.changePassword(u.id(),r.currentPassword(),r.newPassword());}
    @GetMapping("/me") public UserPrincipal me(@AuthenticationPrincipal UserPrincipal u){return u;}
    public record LoginRequest(@Email @NotBlank String email,@Size(min=10,max=200) String password,@NotBlank @Size(max=200) String deviceId){}
    public record RefreshRequest(@NotBlank String refreshToken,@NotBlank String deviceId){}
    public record LogoutRequest(@NotBlank String refreshToken){}
    public record ChangePasswordRequest(@NotBlank String currentPassword,@Size(min=12,max=200) String newPassword){}
}
