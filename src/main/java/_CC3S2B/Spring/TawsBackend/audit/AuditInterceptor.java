package _CC3S2B.Spring.TawsBackend.audit;

import _CC3S2B.Spring.TawsBackend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

    @Value("${spring.application.name:TawsBackend}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        AuditContext.init();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            AuditContext.setUserId(user.getUserId() != null ? user.getUserId().longValue() : 0L);
        } else {
            AuditContext.setUserId(0L);
        }

        AuditContext.setSourceIp(normalizeIp(request.getRemoteAddr()));
        AuditContext.setHttpMethod(request.getMethod());
        AuditContext.setEndpoint(request.getRequestURI());
        AuditContext.setInteractionId(UUID.randomUUID());
        AuditContext.setAppVersion(appVersion);

        log.debug("[Audit] Context initialized - userId={} sourceIp={} endpoint={}",
                AuditContext.getUserId(), AuditContext.getSourceIp(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        AuditContext.clear();
    }

    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "0.0.0.0";
        }

        int percentIndex = ip.indexOf('%');
        if (percentIndex != -1) {
            ip = ip.substring(0, percentIndex);
        }

        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return ip;
        }
    }
}
