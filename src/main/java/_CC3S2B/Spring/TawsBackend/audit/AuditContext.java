package _CC3S2B.Spring.TawsBackend.audit;

import java.util.UUID;

public final class AuditContext {

    private static final ThreadLocal<AuditContext> HOLDER = new ThreadLocal<>();

    private Long userId;
    private String sourceIp;
    private String httpMethod;
    private String endpoint;
    private UUID interactionId;
    private String appVersion;

    private AuditContext() {}

    public static void init() {
        HOLDER.set(new AuditContext());
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long getUserId() {
        AuditContext ctx = HOLDER.get();
        return ctx != null ? ctx.userId : 0L;
    }

    public static String getSourceIp() {
        AuditContext ctx = HOLDER.get();
        return ctx != null ? ctx.sourceIp : null;
    }

    public static String getHttpMethod() {
        AuditContext ctx = HOLDER.get();
        return ctx != null ? ctx.httpMethod : null;
    }

    public static String getEndpoint() {
        AuditContext ctx = HOLDER.get();
        return ctx != null ? ctx.endpoint : null;
    }

    public static UUID getInteractionId() {
        AuditContext ctx = HOLDER.get();
        return ctx != null ? ctx.interactionId : null;
    }

    public static String getAppVersion() {
        AuditContext ctx = HOLDER.get();
        return ctx != null ? ctx.appVersion : null;
    }

    public static void setUserId(Long userId) {
        AuditContext ctx = HOLDER.get();
        if (ctx != null) ctx.userId = userId;
    }

    public static void setSourceIp(String sourceIp) {
        AuditContext ctx = HOLDER.get();
        if (ctx != null) ctx.sourceIp = sourceIp;
    }

    public static void setHttpMethod(String httpMethod) {
        AuditContext ctx = HOLDER.get();
        if (ctx != null) ctx.httpMethod = httpMethod;
    }

    public static void setEndpoint(String endpoint) {
        AuditContext ctx = HOLDER.get();
        if (ctx != null) ctx.endpoint = endpoint;
    }

    public static void setInteractionId(UUID interactionId) {
        AuditContext ctx = HOLDER.get();
        if (ctx != null) ctx.interactionId = interactionId;
    }

    public static void setAppVersion(String appVersion) {
        AuditContext ctx = HOLDER.get();
        if (ctx != null) ctx.appVersion = appVersion;
    }
}
