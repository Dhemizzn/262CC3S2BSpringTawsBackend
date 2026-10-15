package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
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
}