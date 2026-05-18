package com.hostelfinder.repository;

import com.hostelfinder.entity.HostelOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HostelOwnerRepository extends JpaRepository<HostelOwner, Long> {
}
