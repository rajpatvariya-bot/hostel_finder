package com.hostelfinder.controller;

import com.hostelfinder.dto.InquiryDtos;
import com.hostelfinder.entity.Area;
import com.hostelfinder.entity.City;
import com.hostelfinder.entity.Hostel;
import com.hostelfinder.entity.HostelRoomType;
import com.hostelfinder.entity.Inquiry;
import com.hostelfinder.entity.User;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.HostelRepository;
import com.hostelfinder.repository.InquiryRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentInquiryControllerTest {

  @Test
  void createPersistsSelectedRoomTypeWhenProvided() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    InquiryRepository inquiryRepository = mock(InquiryRepository.class);
    StudentInquiryController controller = new StudentInquiryController(hostelRepository, inquiryRepository);

    User student = new User();
    student.setId(21L);
    student.setRole("STUDENT");

    Hostel hostel = buildPublishedHostel();
    HostelRoomType roomType = new HostelRoomType();
    roomType.setId(301L);
    roomType.setHostel(hostel);
    roomType.setRoomType("2 Bed Room");
    roomType.setPricePerMonth(BigDecimal.valueOf(6500));
    roomType.setTotalRooms(8);
    roomType.setAvailableRooms(3);
    hostel.getRoomTypes().add(roomType);

    when(hostelRepository.findByIdFull(55L)).thenReturn(Optional.of(hostel));
    AtomicReference<Inquiry> savedInquiry = new AtomicReference<>();
    when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> {
      Inquiry inquiry = invocation.getArgument(0);
      savedInquiry.set(inquiry);
      return inquiry;
    });

    controller.create(
      new InquiryDtos.CreateInquiryRequest(55L, 1, "", "BOOKING", 301L),
      student
    );

    Inquiry saved = savedInquiry.get();
    assertEquals(301L, saved.getRoomType().getId());
    assertEquals(55L, saved.getHostel().getId());
  }

  @Test
  void createRejectsRoomTypeThatDoesNotBelongToHostel() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    InquiryRepository inquiryRepository = mock(InquiryRepository.class);
    StudentInquiryController controller = new StudentInquiryController(hostelRepository, inquiryRepository);

    User student = new User();
    student.setId(21L);
    student.setRole("STUDENT");

    Hostel hostel = buildPublishedHostel();
    when(hostelRepository.findByIdFull(55L)).thenReturn(Optional.of(hostel));

    ApiException error = assertThrows(
      ApiException.class,
      () -> controller.create(new InquiryDtos.CreateInquiryRequest(55L, 1, "", "BOOKING", 999L), student)
    );

    assertEquals("Selected room type is invalid for this hostel", error.getMessage());
  }

  @Test
  void deleteRemovesOnlyTheStudentsOwnInquiry() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    InquiryRepository inquiryRepository = mock(InquiryRepository.class);
    StudentInquiryController controller = new StudentInquiryController(hostelRepository, inquiryRepository);

    User student = new User();
    student.setId(21L);
    student.setRole("STUDENT");

    Inquiry inquiry = new Inquiry();
    inquiry.setStudent(student);

    when(inquiryRepository.findById(91L)).thenReturn(Optional.of(inquiry));
    doNothing().when(inquiryRepository).delete(inquiry);

    controller.delete(91L, student);

    verify(inquiryRepository).delete(inquiry);
  }

  @Test
  void deleteRejectsAnotherStudentsInquiry() {
    HostelRepository hostelRepository = mock(HostelRepository.class);
    InquiryRepository inquiryRepository = mock(InquiryRepository.class);
    StudentInquiryController controller = new StudentInquiryController(hostelRepository, inquiryRepository);

    User student = new User();
    student.setId(21L);
    student.setRole("STUDENT");

    User otherStudent = new User();
    otherStudent.setId(99L);

    Inquiry inquiry = new Inquiry();
    inquiry.setStudent(otherStudent);

    when(inquiryRepository.findById(91L)).thenReturn(Optional.of(inquiry));

    ApiException error = assertThrows(ApiException.class, () -> controller.delete(91L, student));

    assertEquals("You can only delete your own inquiries", error.getMessage());
  }

  private Hostel buildPublishedHostel() {
    City city = new City();
    city.setName("Indore");

    Area area = new Area();
    area.setName("Vijay Nagar");
    area.setCity(city);

    Hostel hostel = new Hostel();
    hostel.setId(55L);
    hostel.setCity(city);
    hostel.setArea(area);
    hostel.setHostelName("Galaxy Girls PG");
    hostel.setAddressLine("Scheme 54");
    hostel.setGenderType("GIRLS");
    hostel.setStatus("PUBLISHED");
    hostel.setMessAvailable(true);
    return hostel;
  }
}
