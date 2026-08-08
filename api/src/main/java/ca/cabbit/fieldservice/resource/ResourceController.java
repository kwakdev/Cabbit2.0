package ca.cabbit.fieldservice.resource;

import ca.cabbit.fieldservice.security.UserPrincipal;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

@RestController @RequestMapping("/api/v1/resources")
public class ResourceController {
    private final ResourceService service; public ResourceController(ResourceService service){this.service=service;}
    @GetMapping("/{type}") public ResourceService.PageView list(@PathVariable String type,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size,@AuthenticationPrincipal UserPrincipal u){return service.list(type,u,page,size);}
    @GetMapping("/{type}/{id}") public ResourceService.RecordView get(@PathVariable String type,@PathVariable UUID id,@AuthenticationPrincipal UserPrincipal u){return service.get(type,id,u);}
    @PostMapping("/{type}") public ResponseEntity<ResourceService.RecordView> create(@PathVariable String type,@RequestBody Mutation body,@AuthenticationPrincipal UserPrincipal u){ResourceService.RecordView v=service.create(type,body.id(),body.data(),u);return ResponseEntity.created(URI.create("/api/v1/resources/"+type+"/"+v.id())).eTag(String.valueOf(v.version())).body(v);}
    @PutMapping("/{type}/{id}") public ResponseEntity<ResourceService.RecordView> update(@PathVariable String type,@PathVariable UUID id,@RequestBody Mutation body,@AuthenticationPrincipal UserPrincipal u){ResourceService.RecordView v=service.update(type,id,Objects.requireNonNullElse(body.version(),-1L),body.data(),u);return ResponseEntity.ok().eTag(String.valueOf(v.version())).body(v);}
    @DeleteMapping("/{type}/{id}") @PreAuthorize("hasAnyRole('ADMINISTRATOR','MANAGER')") public ResponseEntity<Void> archive(@PathVariable String type,@PathVariable UUID id,@AuthenticationPrincipal UserPrincipal u){service.archive(type,id,u);return ResponseEntity.noContent().build();}
    public record Mutation(UUID id,Long version,Map<String,Object> data){}
}

