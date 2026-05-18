package com.hostelfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "facilities")
public class Facility {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "facility_id")
  private Integer id;

  @Column(nullable = false, unique = true, length = 60)
  private String name;

  public Integer getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}

