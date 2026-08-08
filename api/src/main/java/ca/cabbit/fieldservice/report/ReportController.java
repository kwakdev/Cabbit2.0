package ca.cabbit.fieldservice.report;

import ca.cabbit.fieldservice.domain.AuditEvent;
import ca.cabbit.fieldservice.repo.AuditEventRepository;
import ca.cabbit.fieldservice.resource.ResourceService;
import ca.cabbit.fieldservice.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/reports") @PreAuthorize("hasAnyRole('ADMINISTRATOR','MANAGER','OFFICE_WORKER')")
public class ReportController {
    private final ResourceService resources;private final AuditEventRepository audits;
    public ReportController(ResourceService resources,AuditEventRepository audits){this.resources=resources;this.audits=audits;}
    @GetMapping("/dashboard") public Map<String,Object> dashboard(@AuthenticationPrincipal UserPrincipal u){var jobs=resources.list("work-orders",u,0,100).items();long completed=jobs.stream().filter(x->"COMPLETED".equalsIgnoreCase(String.valueOf(x.data().get("status")))).count();long overdue=jobs.stream().filter(x->Boolean.TRUE.equals(x.data().get("overdue"))).count();long incidents=resources.list("incidents",u,0,100).totalElements();return Map.of("jobsToday",jobs.size(),"jobsCompleted",completed,"jobsOverdue",overdue,"activeIncidents",incidents,"pendingTimeApprovals",resources.list("time-entries",u,0,100).items().stream().filter(x->"PENDING".equals(x.data().get("approvalStatus"))).count());}
    @GetMapping(value="/{type}.csv",produces="text/csv") public ResponseEntity<byte[]> csv(@PathVariable String type,@AuthenticationPrincipal UserPrincipal u){StringBuilder b=new StringBuilder("id,type,updatedAt,data\r\n");for(var r:resources.list(type,u,0,100).items())b.append(csv(r.id().toString())).append(',').append(csv(r.type())).append(',').append(csv(r.updatedAt().toString())).append(',').append(csv(r.data().toString())).append("\r\n");return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+type+"-"+LocalDate.now()+".csv\"").body(b.toString().getBytes(StandardCharsets.UTF_8));}
    @GetMapping("/audit") public List<AuditEvent> audit(@AuthenticationPrincipal UserPrincipal u,@RequestParam(defaultValue="0") int page){return audits.findByCompanyIdOrderByOccurredAtDesc(u.companyId(),PageRequest.of(Math.max(page,0),100)).getContent();}
    private String csv(String v){return "\""+v.replace("\"","\"\"").replace("\r"," ").replace("\n"," ")+"\"";}
}

