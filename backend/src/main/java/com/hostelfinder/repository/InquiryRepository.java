package com.hostelfinder.repository;

import com.hostelfinder.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
  @org.springframework.data.jpa.repository.Query("select i from Inquiry i join fetch i.hostel join fetch i.student where i.student.id = :studentId order by i.id desc")
  List<Inquiry> findByStudentIdOrderByIdDesc(Long studentId);

  @org.springframework.data.jpa.repository.Query("select i from Inquiry i join fetch i.hostel join fetch i.student where i.hostel.ownerUserId = :ownerUserId order by i.id desc")
  List<Inquiry> findByHostel_OwnerUserIdOrderByIdDesc(Long ownerUserId);
}

