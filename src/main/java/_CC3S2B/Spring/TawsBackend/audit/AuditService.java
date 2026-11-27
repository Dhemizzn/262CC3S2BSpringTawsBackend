package _CC3S2B.Spring.TawsBackend.audit;

import _CC3S2B.Spring.TawsBackend.model.audit.AuditChange;
import _CC3S2B.Spring.TawsBackend.model.audit.AuditEvent;
import _CC3S2B.Spring.TawsBackend.repository.audit.AuditChangeRepository;
import _CC3S2B.Spring.TawsBackend.repository.audit.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;
    private final AuditChangeRepository auditChangeRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private HttpServletResponse httpResponse;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(String schema, String table, String recordId, String module) {
        AuditEvent event = buildEvent(schema, table, recordId, module, "INSERT");
        auditEventRepository.save(event);
        log.debug("[Audit] INSERT registered - table={}.{} id={}", schema, table, recordId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(String schema, String table, String recordId, String module,
                          Object oldState, Object newState) {
        AuditEvent event = buildEvent(schema, table, recordId, module, "UPDATE");
        auditEventRepository.save(event);
        List<AuditChange> changes = compareFields(event, oldState, newState);
        if (!changes.isEmpty()) {
            auditChangeRepository.saveAll(changes);
        }
        log.debug("[Audit] UPDATE registered - table={}.{} id={} changes={}",
                schema, table, recordId, changes.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDelete(String schema, String table, String recordId, String module,
                          Object oldState) {
        AuditEvent event = buildEvent(schema, table, recordId, module, "DELETE");
        auditEventRepository.save(event);
        List<AuditChange> changes = captureState(event, oldState);
        if (!changes.isEmpty()) {
            auditChangeRepository.saveAll(changes);
        }
        log.debug("[Audit] DELETE registered - table={}.{} id={} fields={}",
                schema, table, recordId, changes.size());
    }

    private AuditEvent buildEvent(String schema, String table, String recordId,
                                  String module, String operation) {
        return AuditEvent.builder()
                .schemaName(schema)
                .tableName(table)
                .recordId(recordId != null ? recordId : "0")
                .module(module)
                .operation(operation)
                .eventDate(OffsetDateTime.now())
                .result("SUCCESS")
                .userId(nullSafeLong(AuditContext.getUserId()))
                .sourceIp(AuditContext.getSourceIp())
                .httpMethod(AuditContext.getHttpMethod())
                .endpoint(AuditContext.getEndpoint())
                .httpStatus(httpResponse != null ? httpResponse.getStatus() : 200)
                .interactionId(AuditContext.getInteractionId())
                .appVersion(AuditContext.getAppVersion())
                .build();
    }

    private List<AuditChange> compareFields(AuditEvent event, Object oldState, Object newState) {
        List<AuditChange> changes = new ArrayList<>();
        if (oldState == null || newState == null) {
            return changes;
        }
        Class<?> clazz = oldState.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (shouldIgnoreField(field)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object va = field.get(oldState);
                    Object vn = field.get(newState);
                    String strA = serializeValue(va);
                    String strB = serializeValue(vn);
                    if (!Objects.equals(strA, strB)) {
                        changes.add(AuditChange.builder()
                                .event(event)
                                .fieldName(field.getName())
                                .oldValue(strA)
                                .newValue(strB)
                                .build());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("[Audit] Could not access field '{}': {}", field.getName(), e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return changes;
    }

    private List<AuditChange> captureState(AuditEvent event, Object state) {
        List<AuditChange> changes = new ArrayList<>();
        if (state == null) {
            return changes;
        }
        Class<?> clazz = state.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (shouldIgnoreField(field)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    changes.add(AuditChange.builder()
                            .event(event)
                            .fieldName(field.getName())
                            .oldValue(serializeValue(field.get(state)))
                            .newValue(null)
                            .build());
                } catch (IllegalAccessException e) {
                    log.warn("[Audit] Could not access field '{}': {}", field.getName(), e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return changes;
    }

    private boolean shouldIgnoreField(Field field) {
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
            return true;
        }
        return field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(OneToOne.class);
    }

    private String serializeValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String s) {
            return s;
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            if (obj instanceof BigDecimal bd) {
                return bd.stripTrailingZeros().toPlainString();
            }
            return obj.toString();
        }
        if (obj instanceof LocalDate ld) {
            return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (obj instanceof LocalTime lt) {
            return lt.format(DateTimeFormatter.ISO_LOCAL_TIME);
        }
        if (obj instanceof LocalDateTime ldt) {
            return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (obj instanceof OffsetDateTime odt) {
            return odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        if (obj instanceof Map<?, ?> || obj instanceof Iterable<?>) {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (JacksonException e) {
                return obj.toString();
            }
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private long nullSafeLong(Long value) {
        return value != null ? value : 0L;
    }
}
