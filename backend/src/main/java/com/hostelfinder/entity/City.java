package com.hostelfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cities")
public class City {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "city_id")
  private Integer id;

  @Column(nullable = false, unique = true, length = 80)
  private String name;

  public Integer getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}

