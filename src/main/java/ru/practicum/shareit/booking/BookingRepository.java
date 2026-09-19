package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByItem_IdAndStatus(Long itemId, BookingStatus status);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId, Long bookerId,
                                                             BookingStatus status, LocalDateTime moment);
}