package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "dni", schema = "taws")
public class Dni {

    @Id
    @Column(name = "dni_number", columnDefinition = "char(8)")
    private String dniNumber;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}
