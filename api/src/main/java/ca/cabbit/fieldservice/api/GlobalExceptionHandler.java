package ca.cabbit.fieldservice.api;

import ca.cabbit.fieldservice.security.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ApiException.class) ResponseEntity<?> api(ApiException e,HttpServletRequest r){return body(e.status,e.code,e.getMessage(),Map.of(),r);}
    @ExceptionHandler(OptimisticLockingFailureException.class) ResponseEntity<?> optimistic(OptimisticLockingFailureException e,HttpServletRequest r){return body(HttpStatus.CONFLICT,"VERSION_CONFLICT","The record changed on another device",Map.of(),r);}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e,HttpServletRequest r){
        Map<String,String> fields=new LinkedHashMap<>(); for(FieldError f:e.getBindingResult().getFieldErrors())fields.putIfAbsent(f.getField(),f.getDefaultMessage());
        return body(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED","Check the highlighted fields",fields,r);
    }
    @ExceptionHandler(Exception.class) ResponseEntity<?> unknown(Exception e,HttpServletRequest r){log.error("Unhandled API failure",e);return body(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","The request could not be completed",Map.of(),r);}
    private ResponseEntity<?> body(HttpStatus s,String code,String msg,Map<String,String> fields,HttpServletRequest r){
        return ResponseEntity.status(s).body(Map.of("code",code,"message",msg,"fieldErrors",fields,"timestamp",Instant.now().toString(),"requestId",Optional.ofNullable(r.getHeader(RequestIdFilter.HEADER)).orElse("")));
    }
}
