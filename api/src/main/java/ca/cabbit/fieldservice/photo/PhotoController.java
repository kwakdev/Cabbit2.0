package ca.cabbit.fieldservice.photo;

import ca.cabbit.fieldservice.AppProperties;
import ca.cabbit.fieldservice.api.ApiException;
import ca.cabbit.fieldservice.security.UserPrincipal;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController @RequestMapping("/api/v1/photos")
public class PhotoController {
    private static final Set<String> MIME=Set.of("image/jpeg","image/png","image/heic","image/webp");
    private final MinioClient minio;private final AppProperties p;
    public PhotoController(AppProperties p){this.p=p;this.minio=MinioClient.builder().endpoint(p.minioEndpoint()).credentials(p.minioAccessKey(),p.minioSecretKey()).build();}
    @PostMapping("/upload-url") public SignedUrl upload(@RequestBody Request r,@AuthenticationPrincipal UserPrincipal u){if(!MIME.contains(r.contentType()))throw ApiException.badRequest("Unsupported image type");if(r.size()>p.maxUploadBytes())throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE,"PHOTO_TOO_LARGE","Photo exceeds upload limit");String key=u.companyId()+"/"+UUID.randomUUID()+extension(r.contentType());try{String url=minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.PUT).bucket(p.minioBucket()).object(key).expiry(15,TimeUnit.MINUTES).build());return new SignedUrl(key,url,900);}catch(Exception e){throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"PHOTO_STORAGE_UNAVAILABLE","Photo storage is unavailable");}}
    @GetMapping("/view-url") public SignedUrl view(@RequestParam String key,@AuthenticationPrincipal UserPrincipal u){if(!key.startsWith(u.companyId()+"/"))throw ApiException.notFound("Photo not found");try{return new SignedUrl(key,minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(p.minioBucket()).object(key).expiry(15,TimeUnit.MINUTES).build()),900);}catch(Exception e){throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"PHOTO_STORAGE_UNAVAILABLE","Photo storage is unavailable");}}
    private String extension(String mime){return switch(mime){case "image/png"->".png";case "image/heic"->".heic";case "image/webp"->".webp";default->".jpg";};}
    public record Request(String contentType,long size,String kind,String caption){}
    public record SignedUrl(String objectKey,String url,long expiresIn){}
}

