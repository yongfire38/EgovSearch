package egovframework.com.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class AuthorizeFilter extends OncePerRequestFilter {

    private final String secretCode = "eGovFramework";
    private final String errorPage = "/error/403.html";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String secretCodeId = request.getHeader("X-CODE-ID");
        String secretUserId = request.getHeader("X-USER-ID");
        String secretUserNm = request.getHeader("X-USER-NM");
        String secretUniqId = request.getHeader("X-UNIQ-ID");
        
        log.debug("##### AuthorizeFilter secretCodeId >>> {}", secretCodeId);
        log.debug("##### AuthorizeFilter secretUserId >>> {}", secretUserId);
        log.debug("##### AuthorizeFilter secretUserNm >>> {}", secretUserNm);
        log.debug("##### AuthorizeFilter secretUniqId >>> {}", secretUniqId);
        
        String path = request.getRequestURI();
        log.debug("##### AuthorizeFilter path >>> {}", path);
        
        // /ext/ops/ 와 루트경로는 우회
        if (path.startsWith("/ext/ops/") || path.equals("/")) {
            log.debug("##### AuthorizeFilter: Auto adding X-CODE-ID header for path {}", path);
            
            filterChain.doFilter(request, response);
            return;
        }
        
        if (ObjectUtils.isEmpty(secretCode) || !secretCode.equals(secretCodeId)) {
            log.warn("##### Access Denied: Unauthorized Access Attempt");

            // 오류 페이지로 리다이렉트
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendRedirect(request.getContextPath() + errorPage);
            return;
        }
        
        request.setAttribute("secretUserId", secretUserId);
        request.setAttribute("secretUserNm", secretUserNm);
        request.setAttribute("secretUniqId", secretUniqId);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|woff|woff2|ttf|otf|eot|ico|html)$") ||
                path.startsWith("/swagger-ui/") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/");
    }

}
