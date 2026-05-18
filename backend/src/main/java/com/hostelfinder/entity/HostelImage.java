package com.hostelfinder.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hostel_images")
public class HostelImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hostel_id", nullable = false)
  private Hostel hostel;

  @Column(name = "file_url", nullable = false, length = 600)
  private String imageUrl;

  @Column(name = "sort_order", nullable = false)
  private int displayOrder;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Hostel getHostel() { return hostel; }
  public void setHostel(Hostel hostel) { this.hostel = hostel; }

  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

  public int getDisplayOrder() { return displayOrder; }
  public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

  public LocalDateTime getCreatedAt() { return createdAt; }
}
