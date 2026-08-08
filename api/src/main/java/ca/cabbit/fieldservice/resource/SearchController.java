package ca.cabbit.fieldservice.resource;

import ca.cabbit.fieldservice.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/search")
public class SearchController {
    private final ResourceService resources;
    public SearchController(ResourceService resources){this.resources=resources;}
    @GetMapping public List<ResourceService.RecordView> search(@RequestParam String type,@RequestParam String q,@AuthenticationPrincipal UserPrincipal user){
        String needle=q.trim().toLowerCase(Locale.ROOT);if(needle.length()<2)return List.of();
        return resources.list(type,user,0,100).items().stream().filter(r->r.data().values().stream().map(String::valueOf).anyMatch(v->v.toLowerCase(Locale.ROOT).contains(needle))).limit(50).toList();
    }
}
