package ca.cabbit.fieldservice.sync;

import ca.cabbit.fieldservice.domain.SyncReceipt;
import ca.cabbit.fieldservice.repo.SyncReceiptRepository;
import ca.cabbit.fieldservice.resource.ResourceService;
import ca.cabbit.fieldservice.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/v1/sync")
public class SyncController {
    private final ResourceService resources; private final SyncReceiptRepository receipts;
    public SyncController(ResourceService resources,SyncReceiptRepository receipts){this.resources=resources;this.receipts=receipts;}
    @PostMapping @Transactional public SyncResponse sync(@RequestBody SyncRequest request,@AuthenticationPrincipal UserPrincipal user){
        List<Result> results=new ArrayList<>();
        for(Operation op:request.operations()){
            Optional<SyncReceipt> prior=receipts.findById(op.operationId());
            if(prior.isPresent()){results.add(new Result(op.operationId(),prior.get().status,prior.get().details,null));continue;}
            try{
                ResourceService.RecordView record;
                if("DELETE".equalsIgnoreCase(op.action())){resources.archive(op.entityType(),op.entityId(),user);record=null;}
                else record=resources.upsertFromSync(op.entityType(),op.entityId(),op.version(),op.payload(),user);
                receipts.save(new SyncReceipt(op.operationId(),user.companyId(),user.id(),request.deviceId(),op.entityType(),op.entityId(),op.action(),"APPLIED",""));
                results.add(new Result(op.operationId(),"APPLIED",null,record));
            }catch(Exception e){String detail=e.getMessage()==null?"Synchronization failed":e.getMessage();receipts.save(new SyncReceipt(op.operationId(),user.companyId(),user.id(),request.deviceId(),op.entityType(),op.entityId(),op.action(),"CONFLICT",detail));results.add(new Result(op.operationId(),"CONFLICT",detail,null));}
        }
        Instant now=Instant.now();return new SyncResponse(results,resources.changes(user,request.since()==null?now.minusSeconds(86400*30L):request.since()),now);
    }
    public record SyncRequest(String deviceId,Instant since,List<Operation> operations){public SyncRequest{operations=operations==null?List.of():operations;}}
    public record Operation(UUID operationId,String entityType,UUID entityId,String action,Long version,Instant localTimestamp,Map<String,Object> payload){}
    public record Result(UUID operationId,String status,String error,ResourceService.RecordView record){}
    public record SyncResponse(List<Result> results,List<ResourceService.RecordView> changes,Instant serverTime){}
}

