package com.hostelfinder.repository;

import com.hostelfinder.entity.HostelImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostelImageRepository extends JpaRepository<HostelImage, Long> {
  List<HostelImage> findByHostel_IdOrderByDisplayOrderAscIdAsc(Long hostelId);
  long countByHostel_Id(Long hostelId);
  Optional<HostelImage> findByIdAndHostel_Id(Long imageId, Long hostelId);
}
