package com.hostelfinder.dto;

import java.util.List;

public class HostelImageDtos {
  public record HostelImageItem(
    Long id,
    String imageUrl,
    int displayOrder
  ) {}

  public record ReorderHostelImagesRequest(List<Long> imageIds) {}
}
