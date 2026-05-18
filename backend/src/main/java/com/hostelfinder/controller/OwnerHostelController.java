package com.hostelfinder.controller;

import com.hostelfinder.dto.HostelImageDtos.HostelImageItem;
import com.hostelfinder.dto.HostelImageDtos.ReorderHostelImagesRequest;
import com.hostelfinder.dto.OwnerHostelDtos.RoomTypeRequest;
import com.hostelfinder.dto.OwnerHostelDtos.CreateHostelRequest;
import com.hostelfinder.dto.OwnerHostelDtos.UpdateHostelRequest;
import com.hostelfinder.dto.PublicDtos.HostelDetailsResponse;
import com.hostelfinder.dto.PublicDtos.HostelRoomTypeItem;
import com.hostelfinder.entity.*;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.*;
import com.hostelfinder.storage.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/owner/hostels")
@Transactional
public class OwnerHostelController {

  private final HostelRepository hostelRepository;
  private final CityRepository cityRepository;
  private final AreaRepository areaRepository;
  private final FacilityRepository facilityRepository;
  private final HostelImageRepository hostelImageRepository;
  private final FileStorageService fileStorageService;

  private static final int MAX_IMAGE_COUNT = 8;
  private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

  public OwnerHostelController(HostelRepository hostelRepository, CityRepository cityRepository,
      AreaRepository areaRepository, FacilityRepository facilityRepository,
      HostelImageRepository hostelImageRepository, FileStorageService fileStorageService) {
    this.hostelRepository = hostelRepository;
    this.cityRepository = cityRepository;
    this.areaRepository = areaRepository;
    this.facilityRepository = facilityRepository;
    this.hostelImageRepository = hostelImageRepository;
    this.fileStorageService = fileStorageService;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<HostelDetailsResponse> getMyHostels(@AuthenticationPrincipal User user) {
    return hostelRepository.findByOwnerUserId(user.getId()).stream().map(this::mapToResponse).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HostelDetailsResponse createHostel(@AuthenticationPrincipal User user, @RequestBody CreateHostelRequest req) {
    validateHostelRequest(req.hostelName(), req.addressLine(), req.genderType(), req.roomTypes());

    City city = cityRepository.findById(req.cityId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "City not found"));
    Area area = areaRepository.findById(req.areaId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Area not found"));

    Hostel h = new Hostel();
    h.setOwnerUserId(user.getId());
    h.setCity(city);
    h.setArea(area);
    h.setHostelName(req.hostelName().trim());
    h.setAddressLine(req.addressLine().trim());
    h.setDescription(req.description() != null ? req.description().trim() : "");
    h.setImageUrl(normalizeLegacyImageUrl(req.imageUrl()));
    h.setGenderType(req.genderType().trim().toUpperCase(Locale.ROOT));
    h.setMessAvailable(req.messAvailable());
    h.setStatus("DRAFT");
    replaceRoomTypes(h, req.roomTypes());
    h.setFacilities(resolveFacilities(req.facilityIds()));

    h = hostelRepository.save(h);
    hostelRepository.flush();
    return mapToResponse(h);
  }

  @PutMapping("/{id}")
  public HostelDetailsResponse updateHostel(@AuthenticationPrincipal User user, @PathVariable Long id,
      @RequestBody UpdateHostelRequest req) {
    Hostel h = hostelRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));

    if (!h.getOwnerUserId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Not your hostel");
    }

    validateHostelRequest(req.hostelName(), req.addressLine(), req.genderType(), req.roomTypes());

    h.setHostelName(req.hostelName().trim());
    h.setAddressLine(req.addressLine().trim());
    h.setDescription(req.description() != null ? req.description().trim() : "");
    h.setImageUrl(normalizeLegacyImageUrl(req.imageUrl()));
    h.setGenderType(req.genderType().trim().toUpperCase(Locale.ROOT));
    h.setMessAvailable(req.messAvailable());
    replaceRoomTypes(h, req.roomTypes());
    h.setFacilities(resolveFacilities(req.facilityIds()));

    h = hostelRepository.save(h);
    hostelRepository.flush();
    return mapToResponse(h);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteHostel(@AuthenticationPrincipal User user, @PathVariable Long id) {
    Hostel h = hostelRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));

    if (!h.getOwnerUserId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Not your hostel");
    }

    hostelRepository.delete(h);
  }

  @PatchMapping("/{id}/publish")
  public HostelDetailsResponse publishHostel(@AuthenticationPrincipal User user, @PathVariable Long id) {
    Hostel h = hostelRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));

    if (!h.getOwnerUserId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Not your hostel");
    }

    if ("PUBLISHED".equals(h.getStatus())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Hostel is already published");
    }

    h.setStatus("PUBLISHED");
    h = hostelRepository.save(h);
    hostelRepository.flush();
    return mapToResponse(h);
  }

  @GetMapping("/{id}/images")
  @Transactional(readOnly = true)
  public List<HostelImageItem> getHostelImages(@AuthenticationPrincipal User user, @PathVariable Long id) {
    Hostel hostel = requireOwnedHostel(user, id);
    return buildImageItems(hostel);
  }

  @PostMapping("/{id}/images")
  @ResponseStatus(HttpStatus.CREATED)
  public List<HostelImageItem> uploadHostelImages(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @RequestParam("files") List<MultipartFile> files) {
    Hostel hostel = requireOwnedHostel(user, id);
    validateUploadFiles(hostel, files);

    List<HostelImage> existingImages = new ArrayList<>(sortedImages(hostel));
    int nextDisplayOrder = existingImages.size();

    for (MultipartFile file : files) {
      try {
        FileStorageService.StoredFile storedFile = fileStorageService.save(file, "hostels");
        HostelImage hostelImage = new HostelImage();
        hostelImage.setHostel(hostel);
        hostelImage.setImageUrl(storedFile.url());
        hostelImage.setDisplayOrder(nextDisplayOrder++);
        hostel.getImages().add(hostelImage);
      } catch (IOException e) {
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
      }
    }

    applyCoverImage(hostel);
    hostelRepository.save(hostel);
    hostelRepository.flush();
    return buildImageItems(hostel);
  }

  @DeleteMapping("/{hostelId}/images/{imageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteHostelImage(
      @AuthenticationPrincipal User user,
      @PathVariable Long hostelId,
      @PathVariable Long imageId) {
    Hostel hostel = requireOwnedHostel(user, hostelId);
    HostelImage image = hostelImageRepository.findByIdAndHostel_Id(imageId, hostelId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found"));

    hostel.getImages().removeIf(existing -> existing.getId().equals(imageId));
    hostelImageRepository.delete(image);
    try {
      fileStorageService.deleteByUrl(image.getImageUrl());
    } catch (IOException ignored) {
      // Keep the DB deletion even if local cleanup misses
    }

    normalizeDisplayOrder(hostel.getImages());
    applyCoverImage(hostel);
    hostelRepository.save(hostel);
  }

  @PatchMapping("/{id}/images/reorder")
  public List<HostelImageItem> reorderHostelImages(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @RequestBody ReorderHostelImagesRequest req) {
    Hostel hostel = requireOwnedHostel(user, id);
    List<HostelImage> images = new ArrayList<>(sortedImages(hostel));
    if (req == null || req.imageIds() == null || req.imageIds().size() != images.size()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Image reorder payload is invalid");
    }

    for (int index = 0; index < req.imageIds().size(); index++) {
      Long imageId = req.imageIds().get(index);
      HostelImage image = images.stream()
        .filter(item -> item.getId().equals(imageId))
        .findFirst()
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Image reorder payload is invalid"));
      image.setDisplayOrder(index);
    }

    images.sort(Comparator.comparingInt(HostelImage::getDisplayOrder).thenComparing(HostelImage::getId));
    applyCoverImage(hostel);
    hostelRepository.save(hostel);
    return buildImageItems(hostel);
  }

  private HostelDetailsResponse mapToResponse(Hostel h) {
    List<HostelImageItem> images = buildImageItems(h);
    List<String> imageUrls = images.stream().map(HostelImageItem::imageUrl).toList();
    List<HostelRoomTypeItem> roomTypes = h.getRoomTypes().stream().map(this::mapRoomTypeItem).toList();
    return new HostelDetailsResponse(
        h.getId(),
        h.getHostelName(),
        h.getCity().getName(),
        h.getArea().getName(),
        h.getAddressLine(),
        h.getPricePerMonth(),
        h.getGenderType(),
        h.isMessAvailable(),
        h.getAvailableRooms(),
        h.getTotalRooms(),
        h.getFacilities().stream().map(Facility::getName).toList(),
        roomTypes,
        imageUrls,
        images,
        h.getDescription(),
        h.getStatus()
    );
  }

  private Hostel requireOwnedHostel(User user, Long hostelId) {
    Hostel hostel = hostelRepository.findById(hostelId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));

    if (!hostel.getOwnerUserId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Not your hostel");
    }
    return hostel;
  }

  private void validateUploadFiles(Hostel hostel, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Select at least one image to upload.");
    }

    long existingCount = hostelImageRepository.countByHostel_Id(hostel.getId());
    if (existingCount + files.size() > MAX_IMAGE_COUNT) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum 8 hostel images are allowed.");
    }

    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Empty image files are not allowed.");
      }
      if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Each image must be 5 MB or smaller.");
      }
      if (!fileStorageService.isSupportedImageFilename(file.getOriginalFilename())) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported image format. Use jpg, jpeg, png, or webp.");
      }
    }
  }

  private List<HostelImage> sortedImages(Hostel hostel) {
    return hostelImageRepository.findByHostel_IdOrderByDisplayOrderAscIdAsc(hostel.getId()).stream()
      .sorted(Comparator.comparingInt(HostelImage::getDisplayOrder).thenComparing(HostelImage::getId, Comparator.nullsLast(Long::compareTo)))
      .toList();
  }

  private List<HostelImageItem> buildImageItems(Hostel hostel) {
    return sortedImages(hostel).stream()
      .map(image -> new HostelImageItem(image.getId(), image.getImageUrl(), image.getDisplayOrder()))
      .toList();
  }

  private void normalizeDisplayOrder(List<HostelImage> images) {
    List<HostelImage> ordered = images.stream()
      .sorted(Comparator.comparingInt(HostelImage::getDisplayOrder).thenComparing(HostelImage::getId, Comparator.nullsLast(Long::compareTo)))
      .toList();
    for (int index = 0; index < ordered.size(); index++) {
      ordered.get(index).setDisplayOrder(index);
    }
  }

  private void applyCoverImage(Hostel hostel) {
    List<HostelImage> ordered = sortedImages(hostel);
    hostel.setImageUrl(ordered.isEmpty() ? normalizeLegacyImageUrl(hostel.getImageUrl()) : ordered.get(0).getImageUrl());
    hostel.getImages().clear();
    hostel.getImages().addAll(ordered);
  }

  private String normalizeLegacyImageUrl(String imageUrl) {
    if (imageUrl == null) return null;
    String trimmed = imageUrl.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private void validateHostelRequest(String hostelName, String addressLine, String genderType, List<RoomTypeRequest> roomTypes) {
    if (hostelName == null || hostelName.trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Hostel name is required");
    }
    if (addressLine == null || addressLine.trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Address line is required");
    }
    if (genderType == null || genderType.trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Gender type is required");
    }
    validateRoomTypeRequests(roomTypes);
  }

  private void validateRoomTypeRequests(List<RoomTypeRequest> roomTypes) {
    if (roomTypes == null || roomTypes.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "At least one room type is required");
    }

    Set<String> normalizedNames = new HashSet<>();
    for (RoomTypeRequest roomType : roomTypes) {
      if (roomType == null || roomType.roomType() == null || roomType.roomType().trim().isEmpty()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Room type name is required");
      }
      if (roomType.pricePerMonth() == null || roomType.pricePerMonth().compareTo(BigDecimal.ZERO) <= 0) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Room type price must be greater than 0");
      }
      if (roomType.totalRooms() <= 0) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Room type total rooms must be greater than 0");
      }
      if (roomType.availableRooms() < 0) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Room type available rooms cannot be negative");
      }
      if (roomType.availableRooms() > roomType.totalRooms()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Room type available rooms cannot exceed total rooms");
      }

      String normalizedName = roomType.roomType().trim().toLowerCase(Locale.ROOT);
      if (!normalizedNames.add(normalizedName)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Room type names must be unique within a hostel");
      }
    }
  }

  private void replaceRoomTypes(Hostel hostel, List<RoomTypeRequest> roomTypes) {
    // If this is a new hostel (no id yet) simply add all room types as new entries
    if (hostel.getId() == null) {
      hostel.getRoomTypes().clear();
      for (RoomTypeRequest roomTypeRequest : roomTypes) {
        HostelRoomType roomType = new HostelRoomType();
        roomType.setHostel(hostel);
        roomType.setRoomType(roomTypeRequest.roomType().trim());
        roomType.setPricePerMonth(roomTypeRequest.pricePerMonth());
        roomType.setTotalRooms(roomTypeRequest.totalRooms());
        roomType.setAvailableRooms(roomTypeRequest.availableRooms());
        hostel.getRoomTypes().add(roomType);
      }
    } else {
      // Merge incoming room types with existing ones.
      // Build maps of existing room types by id and by normalized name so we can
      // update even when the frontend doesn't send the id.
      java.util.Map<Long, HostelRoomType> existingById = new java.util.HashMap<>();
      java.util.Map<String, HostelRoomType> currentByName = new java.util.HashMap<>();
      for (HostelRoomType existing : new ArrayList<>(hostel.getRoomTypes())) {
        if (existing.getId() != null) existingById.put(existing.getId(), existing);
        if (existing.getRoomType() != null) {
          currentByName.put(existing.getRoomType().trim().toLowerCase(Locale.ROOT), existing);
        }
      }

      // Track incoming ids to detect deletions
      java.util.Set<Long> incomingIds = new java.util.HashSet<>();

      for (RoomTypeRequest req : roomTypes) {
        if (req == null) continue;
        Long rid = req.id();
        String name = req.roomType() == null ? null : req.roomType().trim();
        String normalized = name == null ? null : name.toLowerCase(Locale.ROOT);

        if (rid != null && existingById.containsKey(rid)) {
          // update existing by id
          HostelRoomType existing = existingById.get(rid);
          // If name changed, update currentByName map
          String oldNorm = existing.getRoomType() == null ? null : existing.getRoomType().trim().toLowerCase(Locale.ROOT);
          existing.setRoomType(name);
          existing.setPricePerMonth(req.pricePerMonth());
          existing.setTotalRooms(req.totalRooms());
          existing.setAvailableRooms(req.availableRooms());
          if (oldNorm != null && !oldNorm.equals(normalized)) {
            currentByName.remove(oldNorm);
            if (normalized != null) currentByName.put(normalized, existing);
          }
          incomingIds.add(rid);
        } else if (normalized != null && currentByName.containsKey(normalized)) {
          // frontend didn't provide id but room type name matches an existing one -> update
          HostelRoomType existing = currentByName.get(normalized);
          existing.setRoomType(name);
          existing.setPricePerMonth(req.pricePerMonth());
          existing.setTotalRooms(req.totalRooms());
          existing.setAvailableRooms(req.availableRooms());
          if (existing.getId() != null) incomingIds.add(existing.getId());
        } else {
          // new room type — ensure no duplicate in currentByName (may happen if frontend sent duplicates)
          if (normalized != null && currentByName.containsKey(normalized)) {
            HostelRoomType existing = currentByName.get(normalized);
            existing.setPricePerMonth(req.pricePerMonth());
            existing.setTotalRooms(req.totalRooms());
            existing.setAvailableRooms(req.availableRooms());
            if (existing.getId() != null) incomingIds.add(existing.getId());
          } else {
            HostelRoomType roomType = new HostelRoomType();
            roomType.setHostel(hostel);
            roomType.setRoomType(name);
            roomType.setPricePerMonth(req.pricePerMonth());
            roomType.setTotalRooms(req.totalRooms());
            roomType.setAvailableRooms(req.availableRooms());
            hostel.getRoomTypes().add(roomType);
            if (normalized != null) currentByName.put(normalized, roomType);
          }
        }
      }

      // Remove any existing room types not present in incoming list
      hostel.getRoomTypes().removeIf(rt -> rt.getId() != null && !incomingIds.contains(rt.getId()));
    }

    recalculateDerivedHostelFields(hostel);
  }

  private void recalculateDerivedHostelFields(Hostel hostel) {
    BigDecimal minimumPrice = hostel.getRoomTypes().stream()
      .map(HostelRoomType::getPricePerMonth)
      .min(BigDecimal::compareTo)
      .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "At least one room type is required"));

    int totalRooms = hostel.getRoomTypes().stream().mapToInt(HostelRoomType::getTotalRooms).sum();
    int availableRooms = hostel.getRoomTypes().stream().mapToInt(HostelRoomType::getAvailableRooms).sum();

    hostel.setPricePerMonth(minimumPrice);
    hostel.setTotalRooms(totalRooms);
    hostel.setAvailableRooms(availableRooms);
  }

  private Set<Facility> resolveFacilities(List<Integer> facilityIds) {
    return new HashSet<>(facilityRepository.findAllById(facilityIds == null ? List.of() : facilityIds));
  }

  private HostelRoomTypeItem mapRoomTypeItem(HostelRoomType roomType) {
    return new HostelRoomTypeItem(
      roomType.getId(),
      roomType.getRoomType(),
      roomType.getPricePerMonth(),
      roomType.getTotalRooms(),
      roomType.getAvailableRooms()
    );
  }
}
