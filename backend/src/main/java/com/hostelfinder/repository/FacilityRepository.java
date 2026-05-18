package com.hostelfinder.repository;

import com.hostelfinder.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Integer> {
  List<Facility> findAllByOrderByNameAsc();
}

