package _CC3S2B.Spring.TawsBackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "destination")
public class Destination {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "destination_id")
 private Integer destinationId;

 @Column(name = "name")
 private String name;

 protected Destination() {}

 public Integer getDestinationId() {
  return destinationId;
 }

 public String getName() {
  return name;
 }
}