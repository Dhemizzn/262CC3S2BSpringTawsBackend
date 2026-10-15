package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "airplane")
public class Airplane {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "airplane_id")
 private Integer airplaneId;

 @Column(name = "destination_id")
 private Integer destinationId;

 @Column(name = "airline")
 private String airline;

 @Column(name = "round_trip_price")
 private BigDecimal roundTripPrice;
}