package com.hostelfinder.controller;

import com.hostelfinder.dto.HostelImageDtos.HostelImageItem;
import com.hostelfinder.dto.PublicDtos;
import com.hostelfinder.entity.Hostel;
import com.hostelfinder.entity.HostelRoomType;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.AreaRepository;
import com.hostelfinder.repository.CityRepository;
import com.hostelfinder.repository.FacilityRepository;
import com.hostelfinder.repository.HostelImageRepository;
import com.hostelfinder.repository.HostelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/public")
public class PublicController {
  private final AreaRepository areaRepository;
  private final CityRepository cityRepository;
  private final FacilityRepository facilityRepository;
  private final HostelRepository hostelRepository;
  private final HostelImageRepository hostelImageRepository;

  public PublicController(
    AreaRepository areaRepository,
    CityRepository cityRepository,
    FacilityRepository facilityRepository,
    HostelRepository hostelRepository,
    HostelImageRepository hostelImageRepository
  ) {
    this.areaRepository = areaRepository;
    this.cityRepository = cityRepository;
    this.facilityRepository = facilityRepository;
    this.hostelRepository = hostelRepository;
    this.hostelImageRepository = hostelImageRepository;
  }

  @GetMapping("/cities")
  @Transactional(readOnly = true)
  public List<PublicDtos.IdName> cities() {
    return cityRepository.findAllByOrderByNameAsc()
      .stream()
      .map(c -> new PublicDtos.IdName(c.getId(), c.getName()))
      .toList();
  }

  @GetMapping("/areas")
  @Transactional(readOnly = true)
  public List<PublicDtos.IdName> areas(@RequestParam Integer cityId) {
    return areaRepository.findByCity_IdOrderByNameAsc(cityId)
      .stream()
      .map(a -> new PublicDtos.IdName(a.getId(), a.getName()))
      .toList();
  }

  @GetMapping("/facilities")
  @Transactional(readOnly = true)
  public List<PublicDtos.IdName> facilities() {
    return facilityRepository.findAllByOrderByNameAsc()
      .stream()
      .map(f -> new PublicDtos.IdName(f.getId(), f.getName()))
      .toList();
  }

  @GetMapping("/hostels")
  @Transactional(readOnly = true)
  public PublicDtos.HostelSearchResponse hostels(
    @RequestParam Integer cityId,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Integer areaId,
    @RequestParam(required = false) String genderType,
    @RequestParam(required = false) BigDecimal minPrice,
    @RequestParam(required = false) BigDecimal maxPrice,
    @RequestParam(defaultValue = "false") boolean availableOnly,
    @RequestParam(required = false) String facilityIds,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Collection<Integer> facs = parseFacilityIds(facilityIds);
    boolean empty = facs.isEmpty();

    List<Hostel> results = hostelRepository.searchPublic(
      cityId, areaId, genderType, minPrice, maxPrice, availableOnly, empty, facs
    );
    String normalizedKeyword = normalizeKeyword(keyword);
    if (normalizedKeyword != null) {
      results = results.stream()
        .filter(hostel -> matchesKeyword(hostel, normalizedKeyword))
        .toList();
    }

    List<PublicDtos.HostelCard> items = results.stream()
      .limit(size)
      .skip((long) page * size)
      .map(this::mapToHostelCard)
      .toList();
    return new PublicDtos.HostelSearchResponse(items);
  }

  @GetMapping("/hostels/{id}")
  @Transactional(readOnly = true)
  public PublicDtos.HostelDetailsResponse hostelDetails(@PathVariable Long id) {
    Hostel h = hostelRepository.findByIdFull(id)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));

    if (!"PUBLISHED".equalsIgnoreCase(h.getStatus())) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Hostel not found");
    }

    return mapToHostelDetails(h);
  }

  private Collection<Integer> parseFacilityIds(String facilityIds) {
    if (facilityIds == null || facilityIds.isBlank()) return List.of();
    return Arrays.stream(facilityIds.split(","))
      .map(String::trim)
      .filter(s -> !s.isBlank())
      .map(Integer::valueOf)
      .toList();
  }

  private String normalizeKeyword(String keyword) {
    if (keyword == null) return null;
    String trimmed = keyword.trim().toLowerCase(Locale.ROOT);
    return trimmed.isBlank() ? null : trimmed;
  }

  private boolean matchesKeyword(Hostel hostel, String keyword) {
    String haystack = String.join(" ",
      safe(hostel.getHostelName()),
      safe(hostel.getCity() != null ? hostel.getCity().getName() : null),
      safe(hostel.getArea() != null ? hostel.getArea().getName() : null),
      safe(hostel.getAddressLine()),
      safe(hostel.getDescription()),
      safe(hostel.getGenderType()),
      hostel.isMessAvailable() ? "mess food" : "no mess",
      hostel.getRoomTypes().stream()
        .map(roomType -> safe(roomType.getRoomType()))
        .reduce("", (left, right) -> left + " " + right),
      hostel.getFacilities().stream()
        .map(facility -> safe(facility.getName()))
        .reduce("", (left, right) -> left + " " + right)
    ).toLowerCase(Locale.ROOT);

    return Arrays.stream(keyword.split("\\s+"))
      .allMatch(haystack::contains);
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private PublicDtos.HostelCard mapToHostelCard(Hostel hostel) {
    List<String> imageUrls = getImageUrls(hostel);
    String coverImageUrl = imageUrls.isEmpty() ? hostel.getImageUrl() : imageUrls.get(0);
    return new PublicDtos.HostelCard(
      hostel.getId(),
      hostel.getHostelName(),
      hostel.getCity().getName(),
      hostel.getArea().getName(),
      hostel.getPricePerMonth(),
      hostel.getGenderType(),
      hostel.isMessAvailable(),
      hostel.getAvailableRooms(),
      hostel.getTotalRooms(),
      coverImageUrl,
      imageUrls,
      coverImageUrl,
      hostel.getFacilities().stream().map(com.hostelfinder.entity.Facility::getName).limit(6).toList(),
      mapRoomTypes(hostel)
    );
  }

  private PublicDtos.HostelDetailsResponse mapToHostelDetails(Hostel hostel) {
    List<HostelImageItem> images = getUniqueImageItems(hostel);
    List<String> imageUrls = images.stream().map(HostelImageItem::imageUrl).toList();
    if (imageUrls.isEmpty() && hostel.getImageUrl() != null && !hostel.getImageUrl().isBlank()) {
      imageUrls = List.of(hostel.getImageUrl());
    }

    return new PublicDtos.HostelDetailsResponse(
      hostel.getId(),
      hostel.getHostelName(),
      hostel.getCity().getName(),
      hostel.getArea().getName(),
      hostel.getAddressLine(),
      hostel.getPricePerMonth(),
      hostel.getGenderType(),
      hostel.isMessAvailable(),
      hostel.getAvailableRooms(),
      hostel.getTotalRooms(),
      hostel.getFacilities().stream().map(com.hostelfinder.entity.Facility::getName).toList(),
      mapRoomTypes(hostel),
      imageUrls,
      images,
      hostel.getDescription(),
      hostel.getStatus()
    );
  }

  private List<PublicDtos.HostelRoomTypeItem> mapRoomTypes(Hostel hostel) {
    return hostel.getRoomTypes().stream()
      .sorted(Comparator.comparing(HostelRoomType::getId, Comparator.nullsLast(Long::compareTo)))
      .map(roomType -> new PublicDtos.HostelRoomTypeItem(
        roomType.getId(),
        roomType.getRoomType(),
        roomType.getPricePerMonth(),
        roomType.getTotalRooms(),
        roomType.getAvailableRooms()
      ))
      .toList();
  }

  private List<String> getImageUrls(Hostel hostel) {
    List<String> imageUrls = getUniqueImageItems(hostel).stream()
      .map(HostelImageItem::imageUrl)
      .toList();
    if (imageUrls.isEmpty() && hostel.getImageUrl() != null && !hostel.getImageUrl().isBlank()) {
      return List.of(hostel.getImageUrl());
    }
    return imageUrls;
  }

  private List<HostelImageItem> getUniqueImageItems(Hostel hostel) {
    LinkedHashMap<String, HostelImageItem> unique = new LinkedHashMap<>();
    hostelImageRepository.findByHostel_IdOrderByDisplayOrderAscIdAsc(hostel.getId()).stream()
      .sorted(Comparator.comparingInt(com.hostelfinder.entity.HostelImage::getDisplayOrder).thenComparing(com.hostelfinder.entity.HostelImage::getId))
      .forEach(image -> unique.putIfAbsent(
        image.getId() + ":" + image.getImageUrl(),
        new HostelImageItem(image.getId(), image.getImageUrl(), image.getDisplayOrder())
      ));
    return List.copyOf(unique.values());
  }
}
