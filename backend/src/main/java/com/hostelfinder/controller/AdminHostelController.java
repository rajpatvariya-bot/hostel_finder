package com.hostelfinder.controller;

import com.hostelfinder.repository.HostelRepository;
import com.hostelfinder.entity.Hostel;
import com.hostelfinder.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/hostels")
public class AdminHostelController {
    
    private final HostelRepository hostelRepository;

    public AdminHostelController(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    @GetMapping
    public List<AdminHostelItem> getAllHostels() {
        return hostelRepository.findAll().stream()
            .map(h -> new AdminHostelItem(
                h.getId(),
                h.getHostelName(),
                h.getArea() != null ? h.getArea().getName() : null,
                h.getCity() != null ? h.getCity().getName() : null,
                h.getPricePerMonth(),
                h.getStatus()
            ))
            .toList();
    }

    @PatchMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        Hostel h = hostelRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));
        h.setStatus(req.status());
        hostelRepository.save(h);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHostel(@PathVariable Long id) {
        Hostel h = hostelRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));
        hostelRepository.delete(h);
    }

    public record StatusRequest(String status) {}
    public record AdminHostelItem(
        Long id,
        String hostelName,
        String areaName,
        String cityName,
        BigDecimal pricePerMonth,
        String status
    ) {}
}
