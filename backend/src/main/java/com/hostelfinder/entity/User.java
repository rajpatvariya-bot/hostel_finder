package com.hostelfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String role; // ADMIN/OWNER/STUDENT

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, unique = true, length = 180)
  private String email;

  @Column(nullable = false, unique = true, length = 20)
  private String phone;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column
  private String gender; // MALE/FEMALE/OTHER

  @Column
  private Integer age;

  @Column
  private String address;

  @Column(nullable = false)
  private String status; // ACTIVE/BLOCKED

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }

  public Integer getAge() { return age; }
  public void setAge(Integer age) { this.age = age; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}

