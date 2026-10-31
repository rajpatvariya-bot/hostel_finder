package com.hostelfinder.controller;

import com.hostelfinder.dto.PublicDtos;
import com.hostelfinder.entity.Area;
import com.hostelfinder.entity.City;
import com.hostelfinder.entity.Hostel;
import com.hostelfinder.entity.HostelRoomType;
import com.hostelfinder.repository.AreaRepository;
import com.hostelfinder.repository.CityRepository;
import com.hostelfinder.repository.FacilityRepository;
import com.hostelfinder.repository.HostelImageRepository;
import com.hostelfinder.repository.HostelRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicControllerTest {

  @Test
  void hostelsDeduplicatesRoomTypesBeforeBuildingCards() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    CityRepository cityRepository = mock(CityRepository.class);
    AreaRepository areaRepository = mock(AreaRepository.class);
    FacilityRepository facilityRepository = mock(FacilityRepository.class);
    HostelImageRepository hostelImageRepository = mock(HostelImageRepository.class);

    PublicController controller = new PublicController(
      areaRepository,
      cityRepository,
      facilityRepository,
      hostelRepository,
      hostelImageRepository
    );

    City city = new City();
    city.setName("Indore");

    Area area = new Area();
    area.setName("Vijay Nagar");
    area.setCity(city);

    Hostel hostel = new Hostel();
    hostel.setId(77L);
    hostel.setCity(city);
    hostel.setArea(area);
    hostel.setHostelName("Galaxy Girls PG");
    hostel.setAddressLine("Scheme 54");
    hostel.setPricePerMonth(BigDecimal.valueOf(7000));
    hostel.setGenderType("GIRLS");
    hostel.setMessAvailable(true);
    hostel.setAvailableRooms(3);
    hostel.setTotalRooms(8);
    hostel.setStatus("PUBLISHED");

    HostelRoomType roomType = new HostelRoomType();
    roomType.setId(301L);
    roomType.setHostel(hostel);
    roomType.setRoomType("2 Bed Room");
    roomType.setPricePerMonth(BigDecimal.valueOf(7000));
    roomType.setTotalRooms(8);
    roomType.setAvailableRooms(3);

    hostel.setRoomTypes(List.of(roomType, roomType));

    when(hostelRepository.searchPublic(1, null, null, null, null, false, true, List.of()))
      .thenReturn(List.of(hostel));
    when(hostelImageRepository.findByHostel_IdOrderByDisplayOrderAscIdAsc(77L))
      .thenReturn(List.of());

    PublicDtos.HostelSearchResponse response = controller.hostels(
      1,
      null,
      null,
      null,
      null,
      null,
      false,
      null,
      0,
      10
    );

    assertEquals(1, response.items().size());
    assertEquals(
      List.of(new PublicDtos.HostelRoomTypeItem(301L, "2 Bed Room", BigDecimal.valueOf(7000), 8, 3)),
      response.items().get(0).roomTypes()
    );
  }
}
