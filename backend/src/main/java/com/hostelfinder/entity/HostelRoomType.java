package com.hostelfinder.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hostel_room_types")
public class HostelRoomType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "room_type_id")
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "hostel_id", nullable = false)
  private Hostel hostel;

  @Column(name = "room_type", nullable = false, length = 120)
  private String roomType;

  @Column(name = "price_per_month", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerMonth;

  @Column(name = "total_rooms", nullable = false)
  private int totalRooms;

  @Column(name = "available_rooms", nullable = false)
  private int availableRooms;

  public void setId(Long id) { this.id = id; }
  public Long getId() { return id; }
  public Hostel getHostel() { return hostel; }
  public void setHostel(Hostel hostel) { this.hostel = hostel; }
  public String getRoomType() { return roomType; }
  public void setRoomType(String roomType) { this.roomType = roomType; }
  public BigDecimal getPricePerMonth() { return pricePerMonth; }
  public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }
  public int getTotalRooms() { return totalRooms; }
  public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
  public int getAvailableRooms() { return availableRooms; }
  public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
}
