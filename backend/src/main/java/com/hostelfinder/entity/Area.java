package com.hostelfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "areas",
  uniqueConstraints = @UniqueConstraint(name = "uk_areas_city_name", columnNames = {"city_id", "name"}))
public class Area {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "area_id")
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "city_id", nullable = false)
  private City city;

  @Column(nullable = false, length = 80)
  private String name;

  public Integer getId() { return id; }
  public City getCity() { return city; }
  public void setCity(City city) { this.city = city; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}

