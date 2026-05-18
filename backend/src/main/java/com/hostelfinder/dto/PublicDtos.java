package com.hostelfinder.dto;

import com.hostelfinder.dto.HostelImageDtos.HostelImageItem;

import java.math.BigDecimal;
import java.util.List;

public class PublicDtos {

  public record IdName(Integer id, String name) {}

  public record HostelRoomTypeItem(
    Long id,
    String roomType,
    BigDecimal pricePerMonth,
    int totalRooms,
    int availableRooms
  ) {}

  public record HostelCard(
    Long id,
    String hostelName,
    String cityName,
    String areaName,
    BigDecimal pricePerMonth,
    String genderType,
    boolean messAvailable,
    int availableRooms,
    int totalRooms,
    String coverImageUrl,
    List<String> imageUrls,
    String imageUrl,
    List<String> facilities,
    List<HostelRoomTypeItem> roomTypes
  ) {}

  public record HostelSearchResponse(List<HostelCard> items) {}

  public record HostelDetailsResponse(
    Long id,
    String hostelName,
    String cityName,
    String areaName,
    String addressLine,
    BigDecimal pricePerMonth,
    String genderType,
    boolean messAvailable,
    int availableRooms,
    int totalRooms,
    List<String> facilities,
    List<HostelRoomTypeItem> roomTypes,
    List<String> imageUrls,
    List<HostelImageItem> images,
    String description,
    String status
  ) {}
}

