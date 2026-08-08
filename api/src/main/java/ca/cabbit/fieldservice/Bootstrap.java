package ca.cabbit.fieldservice;

import ca.cabbit.fieldservice.domain.*;
import ca.cabbit.fieldservice.repo.AppUserRepository;
import ca.cabbit.fieldservice.repo.ResourceRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Configuration
public class Bootstrap {
    @Bean CommandLineRunner seed(AppUserRepository users,ResourceRecordRepository records,ObjectMapper json,PasswordEncoder passwords,Environment env,AppProperties props){return args->{
        if(props.production() && (props.jwtSecret().startsWith("CHANGE_ME") || "ChangeMe123!".equals(env.getProperty("app.bootstrap-password"))))throw new IllegalStateException("Production cannot start with default credentials");
        AppUser admin;
        if(users.count()==0){UUID company=UUID.fromString("11111111-1111-1111-1111-111111111111");String email=env.getProperty("app.bootstrap-email","admin@cabbit.local");String password=env.getProperty("app.bootstrap-password","ChangeMe123!");admin=users.save(new AppUser(company,email,passwords.encode(password),"Local Administrator",Role.ADMINISTRATOR,true));}
        else admin=users.findAll().getFirst();
        if(!props.production()&&records.count()==0){
            records.save(new ResourceRecord(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),admin.companyId,"customers",json.writeValueAsString(Map.of("name","Maple Property Services","phone","555-0142")),admin.id));
            records.save(new ResourceRecord(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),admin.companyId,"locations",json.writeValueAsString(Map.of("name","Greenhouse 2","address","125 Cedar Lane","customerId","aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),admin.id));
            records.save(new ResourceRecord(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),admin.companyId,"work-orders",json.writeValueAsString(Map.of("workOrderNumber","WO-1001","title","Irrigation inspection","status","ASSIGNED","priority","HIGH","address","125 Cedar Lane","instructions","Inspect all zones and photograph leaks.","scheduledStart",java.time.Instant.now().toString(),"assignedUserIds",List.of(admin.id.toString()))),admin.id));
        }
    };}
}
