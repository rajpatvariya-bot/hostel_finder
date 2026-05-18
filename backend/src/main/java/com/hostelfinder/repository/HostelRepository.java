package com.hostelfinder.repository;

import com.hostelfinder.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface HostelRepository extends JpaRepository<Hostel, Long> {

  @Query("""
    select distinct h from Hostel h
      left join fetch h.roomTypes
      left join fetch h.facilities
      join fetch h.city
      join fetch h.area
    where h.status = 'PUBLISHED'
      and h.city.id = :cityId
      and (:areaId is null or h.area.id = :areaId)
      and (:genderType is null or h.genderType = :genderType)
      and (:minPrice is null or h.pricePerMonth >= :minPrice)
      and (:maxPrice is null or h.pricePerMonth <= :maxPrice)
      and (:availableOnly = false or h.availableRooms > 0)
      and (:facilityIdsEmpty = true or EXISTS (select 1 from h.facilities f2 where f2.id in :facilityIds))
    order by h.pricePerMonth asc
  """)
  List<Hostel> searchPublic(
    @Param("cityId") Integer cityId,
    @Param("areaId") Integer areaId,
    @Param("genderType") String genderType,
    @Param("minPrice") BigDecimal minPrice,
    @Param("maxPrice") BigDecimal maxPrice,
    @Param("availableOnly") boolean availableOnly,
    @Param("facilityIdsEmpty") boolean facilityIdsEmpty,
    @Param("facilityIds") Collection<Integer> facilityIds
  );

  @Query("select distinct h from Hostel h left join fetch h.roomTypes left join fetch h.facilities join fetch h.city join fetch h.area where h.id = :id")
  java.util.Optional<Hostel> findByIdFull(@Param("id") Long id);

  @Query("select distinct h from Hostel h left join fetch h.roomTypes left join fetch h.facilities join fetch h.city join fetch h.area where h.ownerUserId = :ownerUserId")
  List<Hostel> findByOwnerUserId(@Param("ownerUserId") Long ownerUserId);
}

