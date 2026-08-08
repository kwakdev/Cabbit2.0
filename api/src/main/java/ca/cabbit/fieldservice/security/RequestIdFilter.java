package ca.cabbit.fieldservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER="X-Request-Id";
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String id=request.getHeader(HEADER);
        if (id==null || !id.matches("[A-Za-z0-9._-]{1,100}")) id=UUID.randomUUID().toString();
        response.setHeader(HEADER,id); MDC.put("requestId",id);
        try { chain.doFilter(request,response); } finally { MDC.remove("requestId"); }
    }
}

