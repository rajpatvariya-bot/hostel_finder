package com.hostelfinder.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hostels")
public class Hostel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "hostel_id")
  private Long id;

  @Column(name = "owner_user_id", nullable = false)
  private Long ownerUserId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "city_id", nullable = false)
  private City city;

  @ManyToOne(optional = false)
  @JoinColumn(name = "area_id", nullable = false)
  private Area area;

  @Column(name = "hostel_name", nullable = false, length = 140)
  private String hostelName;

  @Column(name = "address_line", nullable = false, length = 255)
  private String addressLine;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "image_url", length = 500)
  private String imageUrl;


  @Column(name = "price_per_month", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerMonth;

  @Column(name = "gender_type", nullable = false)
  private String genderType; // BOYS/GIRLS/COED

  @Column(name = "mess_available", nullable = false)
  private boolean messAvailable;

  @Column(nullable = false)
  private String status; // DRAFT/PUBLISHED/BLOCKED

  @Column(name = "total_rooms", nullable = false)
  private int totalRooms;

  @Column(name = "available_rooms", nullable = false)
  private int availableRooms;

  @ManyToMany
  @JoinTable(
    name = "hostel_facilities",
    joinColumns = @JoinColumn(name = "hostel_id"),
    inverseJoinColumns = @JoinColumn(name = "facility_id")
  )
  private java.util.Set<Facility> facilities = new java.util.HashSet<>();

  @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("displayOrder ASC, id ASC")
  private java.util.List<HostelImage> images = new java.util.ArrayList<>();

  @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private java.util.List<HostelRoomType> roomTypes = new java.util.ArrayList<>();

  public void setId(Long id) { this.id = id; }
  public Long getId() { return id; }
  public java.util.Set<Facility> getFacilities() { return facilities; }
  public void setFacilities(java.util.Set<Facility> facilities) { this.facilities = facilities; }
  public java.util.List<HostelImage> getImages() { return images; }
  public void setImages(java.util.List<HostelImage> images) { this.images = images; }
  public java.util.List<HostelRoomType> getRoomTypes() { return roomTypes; }
  public void setRoomTypes(java.util.List<HostelRoomType> roomTypes) { this.roomTypes = roomTypes; }
  public Long getOwnerUserId() { return ownerUserId; }
  public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
  public City getCity() { return city; }
  public void setCity(City city) { this.city = city; }
  public Area getArea() { return area; }
  public void setArea(Area area) { this.area = area; }
  public String getHostelName() { return hostelName; }
  public void setHostelName(String hostelName) { this.hostelName = hostelName; }
  public String getAddressLine() { return addressLine; }
  public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public BigDecimal getPricePerMonth() { return pricePerMonth; }
  public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }
  public String getGenderType() { return genderType; }
  public void setGenderType(String genderType) { this.genderType = genderType; }
  public boolean isMessAvailable() { return messAvailable; }
  public void setMessAvailable(boolean messAvailable) { this.messAvailable = messAvailable; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public int getTotalRooms() { return totalRooms; }
  public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
  public int getAvailableRooms() { return availableRooms; }
  public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
}

