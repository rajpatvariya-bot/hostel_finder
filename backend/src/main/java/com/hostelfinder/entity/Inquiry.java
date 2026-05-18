package com.hostelfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inquiries")
public class Inquiry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inquiry_id")
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "hostel_id", nullable = false)
  private Hostel hostel;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_user_id", nullable = false)
  private User student;

  public User getStudent() { return student; }
  public void setStudent(User student) { this.student = student; }

  @Column(name = "requested_room_count", nullable = false)
  private int requestedRoomCount;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Column(name = "request_type", nullable = false, length = 20)
  private String requestType;

  @Column(nullable = false)
  private String status; // PENDING/ACCEPTED/REJECTED/CANCELLED

  @Column(name = "owner_note", length = 400)
  private String ownerNote;

  public Long getId() { return id; }
  public Hostel getHostel() { return hostel; }
  public void setHostel(Hostel hostel) { this.hostel = hostel; }
  public int getRequestedRoomCount() { return requestedRoomCount; }
  public void setRequestedRoomCount(int requestedRoomCount) { this.requestedRoomCount = requestedRoomCount; }
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  public String getRequestType() { return requestType; }
  public void setRequestType(String requestType) { this.requestType = requestType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getOwnerNote() { return ownerNote; }
  public void setOwnerNote(String ownerNote) { this.ownerNote = ownerNote; }
}

