package ca.cabbit.fieldservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.*;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentMap<String,Window> windows=new ConcurrentHashMap<>();
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        boolean login=request.getRequestURI().endsWith("/auth/login");int limit=login?10:600;String key=clientIp(request)+":"+(login?"login":"api");long minute=Instant.now().getEpochSecond()/60;
        Window window=windows.compute(key,(k,v)->v==null||v.minute!=minute?new Window(minute,1):new Window(minute,v.count+1));
        if(window.count>limit){response.setStatus(429);response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.setHeader("Retry-After","60");response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests; try again shortly\"}");return;}
        if(windows.size()>10000)windows.entrySet().removeIf(e->e.getValue().minute<minute-2);chain.doFilter(request,response);
    }
    private String clientIp(HttpServletRequest r){String forwarded=r.getHeader("X-Forwarded-For");return forwarded==null?r.getRemoteAddr():forwarded.split(",",2)[0].trim();}
    private record Window(long minute,int count){}
}
