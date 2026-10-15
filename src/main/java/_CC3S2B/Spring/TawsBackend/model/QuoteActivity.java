package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_activity")
@Data
public class QuoteActivity {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "activity_id")
 private Integer activityId;

 @Column(name = "destination_id")
 private Integer destinationId;

 @Column(name = "name")
 private String name;

 @Column(name = "duration_minutes")
 private Integer durationMinutes;

 @Column(name = "price_per_person")
 private BigDecimal pricePerPerson;
}