package ca.cabbit.fieldservice.resource;

import ca.cabbit.fieldservice.api.ApiException;
import ca.cabbit.fieldservice.domain.*;
import ca.cabbit.fieldservice.repo.*;
import ca.cabbit.fieldservice.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class ResourceService {
    public static final Set<String> TYPES=Set.of("companies","customers","locations","work-orders","assignments","checklist-templates","checklists","photos","materials","material-usage","equipment","equipment-usage","time-entries","break-entries","incidents","notifications","saved-filters","recurring-work-orders","devices","settings");
    private final ResourceRecordRepository records; private final AuditEventRepository audits; private final ObjectMapper json;
    public ResourceService(ResourceRecordRepository records,AuditEventRepository audits,ObjectMapper json){this.records=records;this.audits=audits;this.json=json;}
    private static final Set<String> MANAGEMENT_ONLY=Set.of("companies","customers","locations","assignments","checklist-templates","materials","equipment","recurring-work-orders","settings");
    public PageView list(String type,UserPrincipal user,int page,int size){validType(type);Page<ResourceRecord> p=records.findByCompanyIdAndResourceTypeAndDeletedAtIsNull(user.companyId(),type,PageRequest.of(Math.max(0,page),Math.min(Math.max(size,1),100),Sort.by(Sort.Direction.DESC,"updatedAt")));List<RecordView> visible=p.getContent().stream().map(this::view).filter(r->canRead(r,user)).toList();return new PageView(visible,p.getNumber(),p.getSize(),visible.size(),visible.isEmpty()?0:1);}
    public RecordView get(String type,UUID id,UserPrincipal user){RecordView v=view(find(type,id,user));if(!canRead(v,user))throw ApiException.notFound("Record not found");return v;}
    @Transactional public RecordView create(String type,UUID requestedId,Map<String,Object> data,UserPrincipal user){validType(type);authorizeWrite(type,data,user);UUID id=requestedId==null?UUID.randomUUID():requestedId;if(records.existsById(id))throw ApiException.conflict("Record already exists");validate(type,data);Map<String,Object> owned=new LinkedHashMap<>(data);owned.putIfAbsent("recordedBy",user.id().toString());ResourceRecord r=records.save(new ResourceRecord(id,user.companyId(),type,write(owned),user.id()));audit(user,"CREATE",type,id,owned);return view(r);}
    @Transactional public RecordView update(String type,UUID id,long expectedVersion,Map<String,Object> data,UserPrincipal user){ResourceRecord r=find(type,id,user);authorizeWrite(type,data,user);if(!canRead(view(r),user))throw ApiException.notFound("Record not found");if(r.version!=expectedVersion)throw ApiException.conflict("Record version is stale");validate(type,data);r.data=write(data);r.updatedAt=Instant.now();r.updatedBy=user.id();records.save(r);audit(user,"UPDATE",type,id,data);return view(r);}
    @Transactional public void archive(String type,UUID id,UserPrincipal user){authorizeManager(user);ResourceRecord r=find(type,id,user);r.deletedAt=Instant.now();r.updatedAt=r.deletedAt;r.updatedBy=user.id();records.save(r);audit(user,"ARCHIVE",type,id,Map.of());}
    @Transactional public RecordView upsertFromSync(String type,UUID id,Long version,Map<String,Object> data,UserPrincipal user){
        Optional<ResourceRecord> existing=records.findByIdAndCompanyIdAndResourceTypeAndDeletedAtIsNull(id,user.companyId(),type);
        return existing.isEmpty()?create(type,id,data,user):update(type,id,version==null?-1:version,data,user);
    }
    public List<RecordView> changes(UserPrincipal user,Instant since){return records.findByCompanyIdAndUpdatedAtAfterOrderByUpdatedAt(user.companyId(),since).stream().map(this::view).toList();}
    private ResourceRecord find(String type,UUID id,UserPrincipal u){validType(type);return records.findByIdAndCompanyIdAndResourceTypeAndDeletedAtIsNull(id,u.companyId(),type).orElseThrow(()->ApiException.notFound("Record not found"));}
    private void validType(String t){if(!TYPES.contains(t))throw ApiException.badRequest("Unsupported resource type");}
    private void authorizeWrite(String type,Map<String,Object> data,UserPrincipal user){if(MANAGEMENT_ONLY.contains(type))authorizeManager(user);if("work-orders".equals(type)&&!manager(user)&&!assigned(data,user))throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN,"FORBIDDEN","This work order is not assigned to you");}
    private boolean canRead(RecordView r,UserPrincipal user){return !"work-orders".equals(r.type())||manager(user)||assigned(r.data(),user);}
    private boolean assigned(Map<String,Object> data,UserPrincipal user){Object ids=data.get("assignedUserIds");if(ids instanceof Collection<?> c&&c.stream().map(String::valueOf).anyMatch(user.id().toString()::equals))return true;return user.id().toString().equals(String.valueOf(data.get("assignedTo")))||user.id().toString().equals(String.valueOf(data.get("recordedBy")));}
    private boolean manager(UserPrincipal u){return u.role()==Role.ADMINISTRATOR||u.role()==Role.MANAGER;}
    private void authorizeManager(UserPrincipal u){if(!manager(u))throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN,"FORBIDDEN","Manager access is required");}
    private void validate(String type,Map<String,Object> data){if(data==null||data.isEmpty())throw ApiException.badRequest("data must not be empty");if("work-orders".equals(type)){require(data,"title");require(data,"status");}if("customers".equals(type))require(data,"name");if("incidents".equals(type)){require(data,"severity");require(data,"description");}}
    private void require(Map<String,Object>d,String key){if(!d.containsKey(key)||String.valueOf(d.get(key)).isBlank())throw ApiException.badRequest(key+" is required");}
    private String write(Object data){try{return json.writeValueAsString(data);}catch(JsonProcessingException e){throw ApiException.badRequest("Invalid JSON payload");}}
    @SuppressWarnings("unchecked") private Map<String,Object> read(String data){try{return json.readValue(data,Map.class);}catch(JsonProcessingException e){throw new IllegalStateException(e);}}
    private RecordView view(ResourceRecord r){return new RecordView(r.id,r.resourceType,read(r.data),r.version,r.createdAt,r.updatedAt,r.deletedAt);}
    private void audit(UserPrincipal u,String action,String type,UUID id,Object detail){audits.save(new AuditEvent(u.companyId(),u.id(),action,type,id,write(detail)));}
    public record RecordView(UUID id,String type,Map<String,Object> data,long version,Instant createdAt,Instant updatedAt,Instant deletedAt){}
    public record PageView(List<RecordView> items,int page,int size,long totalElements,int totalPages){}
}
