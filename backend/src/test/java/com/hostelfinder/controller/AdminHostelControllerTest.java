package com.hostelfinder.controller;

import com.hostelfinder.entity.Hostel;
import com.hostelfinder.entity.Area;
import com.hostelfinder.entity.City;
import com.hostelfinder.repository.HostelRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminHostelControllerTest {

  @Test
  void getAllHostelsReturnsAdminItemsWithoutExposingEntityGraph() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    AdminHostelController controller = new AdminHostelController(hostelRepository);

    City city = new City();
    city.setName("Indore");
    Area area = new Area();
    area.setName("Bhawarkua");
    area.setCity(city);

    Hostel hostel = new Hostel();
    hostel.setCity(city);
    hostel.setArea(area);
    hostel.setHostelName("Shree Boys Hostel");
    hostel.setPricePerMonth(BigDecimal.valueOf(6500));
    hostel.setStatus("PUBLISHED");

    when(hostelRepository.findAll()).thenReturn(List.of(hostel));

    List<AdminHostelController.AdminHostelItem> items = controller.getAllHostels();

    assertEquals(1, items.size());
    assertEquals("Shree Boys Hostel", items.get(0).hostelName());
    assertEquals("Bhawarkua", items.get(0).areaName());
    assertEquals("Indore", items.get(0).cityName());
    assertEquals(BigDecimal.valueOf(6500), items.get(0).pricePerMonth());
    assertEquals("PUBLISHED", items.get(0).status());
  }

  @Test
  void deleteHostelRemovesExistingHostel() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    AdminHostelController controller = new AdminHostelController(hostelRepository);
    Hostel hostel = new Hostel();

    when(hostelRepository.findById(5L)).thenReturn(Optional.of(hostel));

    controller.deleteHostel(5L);

    verify(hostelRepository).delete(hostel);
  }
}
