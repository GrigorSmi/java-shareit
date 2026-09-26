package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start,
                                                           LocalDateTime end, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start,
                                                               LocalDateTime end, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_IdAndStatus(Long itemId, BookingStatus status);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId, Long bookerId,
                                                             BookingStatus status, LocalDateTime moment);
}