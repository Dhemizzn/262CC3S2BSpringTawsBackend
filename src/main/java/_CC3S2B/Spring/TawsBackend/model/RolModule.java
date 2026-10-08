package _CC3S2B.Spring.TawsBackend.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@IdClass(RolModuleId.class)
@Table(name = "role_module")
public class RolModule {
    @Id
    @Column(name = "rol_id")
    private Integer rolId;

    @Id
    @Column(name="module_id")
    private Integer moduleId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at",nullable = true, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at",nullable = true )
    private LocalDateTime updatedAt = LocalDateTime.now();
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class RolModuleId implements Serializable {
    private Integer rolId;
    private Integer idMod;
}