package _CC3S2B.Spring.TawsBackend.model.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "event", schema = "audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "schema_name", nullable = false, length = 100)
    private String schemaName;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "record_id", nullable = false, length = 150)
    private String recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "operation", nullable = false, length = 30)
    private String operation;

    @Column(name = "event_date", nullable = false)
    @Builder.Default
    private OffsetDateTime eventDate = OffsetDateTime.now();

    @ColumnTransformer(write = "?::inet")
    @Column(name = "source_ip", columnDefinition = "inet")
    private String sourceIp;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "result", nullable = false, length = 30)
    private String result;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "interaction_id", columnDefinition = "UUID")
    private UUID interactionId;

    @Column(name = "app_version", length = 50)
    private String appVersion;
}
