package ca.cabbit.fieldservice.auth;

import ca.cabbit.fieldservice.AppProperties;
import ca.cabbit.fieldservice.api.ApiException;
import ca.cabbit.fieldservice.domain.*;
import ca.cabbit.fieldservice.repo.*;
import ca.cabbit.fieldservice.security.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

@Service
public class AuthService {
    private final AppUserRepository users; private final RefreshTokenRepository refreshTokens; private final PasswordEncoder passwords; private final TokenService tokens; private final AppProperties props;
    public AuthService(AppUserRepository users,RefreshTokenRepository refreshTokens,PasswordEncoder passwords,TokenService tokens,AppProperties props){this.users=users;this.refreshTokens=refreshTokens;this.passwords=passwords;this.tokens=tokens;this.props=props;}
    @Transactional public AuthResult login(String email,String password,String deviceId){
        AppUser user=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","Email or password is incorrect"));
        Instant now=Instant.now();
        if(!user.active)throw new ApiException(HttpStatus.UNAUTHORIZED,"ACCOUNT_DISABLED","Account is disabled");
        if(user.lockedUntil!=null&&user.lockedUntil.isAfter(now))throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"ACCOUNT_LOCKED","Try again later");
        if(!passwords.matches(password,user.passwordHash)){
            user.failedLogins++; if(user.failedLogins>=5){user.lockedUntil=now.plus(Duration.ofMinutes(15));user.failedLogins=0;} users.save(user);
            throw new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","Email or password is incorrect");
        }
        user.failedLogins=0;user.lockedUntil=null;users.save(user);return issue(user,deviceId);
    }
    @Transactional public AuthResult refresh(String raw,String deviceId){
        RefreshToken stored=refreshTokens.findByTokenHash(hash(raw)).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_REFRESH_TOKEN","Session has expired"));
        if(stored.revokedAt!=null||stored.expiresAt.isBefore(Instant.now())||!stored.deviceId.equals(deviceId))throw new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_REFRESH_TOKEN","Session has expired");
        stored.revokedAt=Instant.now();refreshTokens.save(stored);
        AppUser user=users.findById(stored.userId).filter(u->u.active).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"ACCOUNT_DISABLED","Account is disabled"));return issue(user,deviceId);
    }
    @Transactional public void logout(String raw){refreshTokens.findByTokenHash(hash(raw)).ifPresent(t->{t.revokedAt=Instant.now();refreshTokens.save(t);});}
    @Transactional public void revokeAll(UUID userId){refreshTokens.revokeAll(userId,Instant.now());}
    @Transactional public void changePassword(UUID userId,String currentPassword,String newPassword){
        if(newPassword.length()<12||!newPassword.matches(".*[A-Z].*")||!newPassword.matches(".*[a-z].*")||!newPassword.matches(".*[0-9].*"))throw ApiException.badRequest("Password must be at least 12 characters and include upper, lower and numeric characters");
        AppUser user=users.findById(userId).orElseThrow(()->ApiException.notFound("User not found"));
        if(!passwords.matches(currentPassword,user.passwordHash))throw new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","Current password is incorrect");
        user.passwordHash=passwords.encode(newPassword);user.mustChangePassword=false;users.save(user);refreshTokens.revokeAll(userId,Instant.now());
    }
    private AuthResult issue(AppUser user,String deviceId){
        String raw=UUID.randomUUID()+"."+UUID.randomUUID();
        refreshTokens.save(new RefreshToken(UUID.randomUUID(),user.id,hash(raw),deviceId,Instant.now().plus(props.refreshTokenDays(),java.time.temporal.ChronoUnit.DAYS)));
        return new AuthResult(tokens.accessToken(user),raw,tokens.accessExpiresInSeconds(),new UserView(user.id,user.companyId,user.email,user.displayName,user.role,user.mustChangePassword));
    }
    private String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    public record AuthResult(String accessToken,String refreshToken,long expiresIn,UserView user){}
    public record UserView(UUID id,UUID companyId,String email,String displayName,Role role,boolean mustChangePassword){}
}
