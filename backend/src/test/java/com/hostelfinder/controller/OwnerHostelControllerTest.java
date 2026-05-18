package com.hostelfinder.controller;

import com.hostelfinder.dto.HostelImageDtos;
import com.hostelfinder.dto.OwnerHostelDtos;
import com.hostelfinder.dto.PublicDtos;
import com.hostelfinder.entity.Area;
import com.hostelfinder.entity.City;
import com.hostelfinder.entity.Hostel;
import com.hostelfinder.entity.HostelImage;
import com.hostelfinder.entity.User;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.AreaRepository;
import com.hostelfinder.repository.CityRepository;
import com.hostelfinder.repository.FacilityRepository;
import com.hostelfinder.repository.HostelImageRepository;
import com.hostelfinder.repository.HostelRepository;
import com.hostelfinder.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OwnerHostelControllerTest {

  @Test
  void uploadHostelImagesRejectsUnsupportedFileType() throws Exception {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    CityRepository cityRepository = mock(CityRepository.class);
    AreaRepository areaRepository = mock(AreaRepository.class);
    FacilityRepository facilityRepository = mock(FacilityRepository.class);
    HostelImageRepository hostelImageRepository = mock(HostelImageRepository.class);
    FileStorageService fileStorageService = mock(FileStorageService.class);

    OwnerHostelController controller = new OwnerHostelController(
      hostelRepository,
      cityRepository,
      areaRepository,
      facilityRepository,
      hostelImageRepository,
      fileStorageService
    );

    User owner = new User();
    owner.setId(11L);
    owner.setRole("OWNER");

    Hostel hostel = new Hostel();
    hostel.setId(77L);
    hostel.setOwnerUserId(owner.getId());

    when(hostelRepository.findById(7L)).thenReturn(Optional.of(hostel));

    MockMultipartFile invalidFile = new MockMultipartFile(
      "files",
      "notes.gif",
      "image/gif",
      new byte[] {1, 2, 3}
    );

    ApiException error = assertThrows(
      ApiException.class,
      () -> controller.uploadHostelImages(owner, 7L, List.of(invalidFile))
    );

    assertEquals("Unsupported image format. Use jpg, jpeg, png, or webp.", error.getMessage());
  }

  @Test
  void getMyHostelsReturnsOrderedGalleryImages() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    CityRepository cityRepository = mock(CityRepository.class);
    AreaRepository areaRepository = mock(AreaRepository.class);
    FacilityRepository facilityRepository = mock(FacilityRepository.class);
    HostelImageRepository hostelImageRepository = mock(HostelImageRepository.class);
    FileStorageService fileStorageService = mock(FileStorageService.class);

    OwnerHostelController controller = new OwnerHostelController(
      hostelRepository,
      cityRepository,
      areaRepository,
      facilityRepository,
      hostelImageRepository,
      fileStorageService
    );

    User owner = new User();
    owner.setId(11L);
    owner.setRole("OWNER");

    City city = new City();
    city.setName("Indore");
    Area area = new Area();
    area.setName("Vijay Nagar");
    area.setCity(city);

    Hostel hostel = new Hostel();
    hostel.setId(77L);
    hostel.setOwnerUserId(owner.getId());
    hostel.setCity(city);
    hostel.setArea(area);
    hostel.setHostelName("Galaxy Girls PG");
    hostel.setAddressLine("Scheme 54");
    hostel.setPricePerMonth(BigDecimal.valueOf(8000));
    hostel.setGenderType("GIRLS");
    hostel.setAvailableRooms(2);
    hostel.setStatus("PUBLISHED");

    HostelImage second = new HostelImage();
    second.setId(22L);
    second.setImageUrl("/uploads/hostels/second.webp");
    second.setDisplayOrder(1);
    second.setHostel(hostel);

    HostelImage first = new HostelImage();
    first.setId(21L);
    first.setImageUrl("/uploads/hostels/first.webp");
    first.setDisplayOrder(0);
    first.setHostel(hostel);

    hostel.setImages(List.of(second, first));

    when(hostelRepository.findByOwnerUserId(owner.getId())).thenReturn(List.of(hostel));
    when(hostelImageRepository.findByHostel_IdOrderByDisplayOrderAscIdAsc(77L)).thenReturn(List.of(second, first));

    List<PublicDtos.HostelDetailsResponse> items = controller.getMyHostels(owner);

    assertEquals(1, items.size());
    assertEquals(List.of("/uploads/hostels/first.webp", "/uploads/hostels/second.webp"), items.get(0).imageUrls());
    assertEquals(
      List.of(
        new HostelImageDtos.HostelImageItem(21L, "/uploads/hostels/first.webp", 0),
        new HostelImageDtos.HostelImageItem(22L, "/uploads/hostels/second.webp", 1)
      ),
      items.get(0).images()
    );
  }

  @Test
  void createHostelReturnsRoomTypesAndDerivedTotals() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    CityRepository cityRepository = mock(CityRepository.class);
    AreaRepository areaRepository = mock(AreaRepository.class);
    FacilityRepository facilityRepository = mock(FacilityRepository.class);
    HostelImageRepository hostelImageRepository = mock(HostelImageRepository.class);
    FileStorageService fileStorageService = mock(FileStorageService.class);

    OwnerHostelController controller = new OwnerHostelController(
      hostelRepository,
      cityRepository,
      areaRepository,
      facilityRepository,
      hostelImageRepository,
      fileStorageService
    );

    User owner = new User();
    owner.setId(11L);
    owner.setRole("OWNER");

    City city = new City();
    city.setName("Indore");

    Area area = new Area();
    area.setName("Vijay Nagar");
    area.setCity(city);

    when(cityRepository.findById(1)).thenReturn(Optional.of(city));
    when(areaRepository.findById(2)).thenReturn(Optional.of(area));
    when(hostelRepository.save(any(Hostel.class))).thenAnswer(invocation -> {
      Hostel hostel = invocation.getArgument(0);
      hostel.setId(55L);
      return hostel;
    });
    doNothing().when(hostelRepository).flush();

    PublicDtos.HostelDetailsResponse response = controller.createHostel(
      owner,
      new OwnerHostelDtos.CreateHostelRequest(
        "Galaxy Girls PG",
        1,
        2,
        "Scheme 54",
        "Walkable from coaching hubs",
        "",
        "GIRLS",
        true,
        List.of(
          new OwnerHostelDtos.RoomTypeRequest("1 Bed Room", BigDecimal.valueOf(9500), 6, 2),
          new OwnerHostelDtos.RoomTypeRequest("2 Bed Room", BigDecimal.valueOf(7000), 8, 3)
        ),
        List.of()
      )
    );

    assertEquals(BigDecimal.valueOf(7000), response.pricePerMonth());
    assertEquals(14, response.totalRooms());
    assertEquals(5, response.availableRooms());
    assertEquals(
      List.of(
        new PublicDtos.HostelRoomTypeItem(null, "1 Bed Room", BigDecimal.valueOf(9500), 6, 2),
        new PublicDtos.HostelRoomTypeItem(null, "2 Bed Room", BigDecimal.valueOf(7000), 8, 3)
      ),
      response.roomTypes()
    );
  }

  @Test
  void updateHostelReplacesRoomTypesAndRecalculatesDerivedFields() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    CityRepository cityRepository = mock(CityRepository.class);
    AreaRepository areaRepository = mock(AreaRepository.class);
    FacilityRepository facilityRepository = mock(FacilityRepository.class);
    HostelImageRepository hostelImageRepository = mock(HostelImageRepository.class);
    FileStorageService fileStorageService = mock(FileStorageService.class);

    OwnerHostelController controller = new OwnerHostelController(
      hostelRepository,
      cityRepository,
      areaRepository,
      facilityRepository,
      hostelImageRepository,
      fileStorageService
    );

    User owner = new User();
    owner.setId(11L);
    owner.setRole("OWNER");

    City city = new City();
    city.setName("Indore");

    Area area = new Area();
    area.setName("Vijay Nagar");
    area.setCity(city);

    Hostel hostel = new Hostel();
    hostel.setId(55L);
    hostel.setOwnerUserId(owner.getId());
    hostel.setCity(city);
    hostel.setArea(area);
    hostel.setHostelName("Galaxy Girls PG");
    hostel.setAddressLine("Scheme 54");
    hostel.setGenderType("GIRLS");
    hostel.setMessAvailable(true);
    hostel.setStatus("DRAFT");

    when(hostelRepository.findById(55L)).thenReturn(Optional.of(hostel));
    when(hostelRepository.save(any(Hostel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    doNothing().when(hostelRepository).flush();

    PublicDtos.HostelDetailsResponse response = controller.updateHostel(
      owner,
      55L,
      new OwnerHostelDtos.UpdateHostelRequest(
        "Galaxy Girls PG",
        "Scheme 54",
        "Updated room options",
        "",
        "GIRLS",
        true,
        List.of(
          new OwnerHostelDtos.RoomTypeRequest("3 Bed Room", BigDecimal.valueOf(6200), 10, 4),
          new OwnerHostelDtos.RoomTypeRequest("4 Bed Room", BigDecimal.valueOf(5400), 5, 1)
        ),
        List.of()
      )
    );

    assertEquals(BigDecimal.valueOf(5400), response.pricePerMonth());
    assertEquals(15, response.totalRooms());
    assertEquals(5, response.availableRooms());
    assertEquals(
      List.of(
        new PublicDtos.HostelRoomTypeItem(null, "3 Bed Room", BigDecimal.valueOf(6200), 10, 4),
        new PublicDtos.HostelRoomTypeItem(null, "4 Bed Room", BigDecimal.valueOf(5400), 5, 1)
      ),
      response.roomTypes()
    );
  }
}
