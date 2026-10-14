package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name="airplane")
public class Airplane {
 @Id
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 @Column(name="airplane_id")
 private Integer airplaneId;
 @Column(name="destination_id")
 private Integer destinationId;
 @Column(name="airline")
 private String airline;
 @Column(name="round_trip_price")
 private BigDecimal roundTripPrice;
 protected Airplane() {}
 public Integer getAirplaneId() { return airplaneId; }
 public Integer getDestinationId() { return destinationId; }
 public String getAirline() { return airline; }
 public BigDecimal getRoundTripPrice() { return roundTripPrice; }
}
