package com.hostelfinder.repository;

import com.hostelfinder.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Integer> {
  List<Area> findByCity_IdOrderByNameAsc(Integer cityId);
}

