package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "hotel")
public class Hotel {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "hotel_id")
 private Integer hotelId;

 @Column(name = "destination_id")
 private Integer destinationId;

 @Column(name = "name")
 private String name;

 @Column(name = "price_per_night")
 private BigDecimal pricePerNight;

 protected Hotel() {}

 public Integer getHotelId() {
  return hotelId;
 }

 public Integer getDestinationId() {
  return destinationId;
 }

 public String getName() {
  return name;
 }

 public BigDecimal getPricePerNight() {
  return pricePerNight;
 }
}