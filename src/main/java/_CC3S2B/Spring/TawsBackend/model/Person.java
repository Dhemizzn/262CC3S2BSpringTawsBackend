package _CC3S2B.Spring.TawsBackend.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "Person")

public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer personId;

    @Column(name = "first_name",nullable = false, length = 50)
    private String firstName;

    @Column(name = "paternal_surname", nullable = false, length = 50)
    private String paternalSurname;

    @Column(name = "maternal_surname", nullable = false, length = 50)
    private String maternalSurname;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false, length = 9)
    private String phone;

    @Column(nullable = false)
    private String direction;

    @Column(unique = true, nullable = false)
    String email;

    @Column(name = "created_at",nullable = true, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at",nullable = true )
    private LocalDateTime updatedAt = LocalDateTime.now();
}
