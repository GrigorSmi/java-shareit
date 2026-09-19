package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = ItemMapper.fromDto(itemDto);
        item.setOwner(owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existing.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь не найдена");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        itemRepository.save(existing);
        return ItemMapper.toDto(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDetailsDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemBookingDto lastBooking = null;
        ItemBookingDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = findLastBooking(itemId);
            nextBooking = findNextBooking(itemId);
        }

        List<CommentDto> comments = commentRepository.findByItem_Id(itemId).stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());

        return new ItemDetailsDto(item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemOwnerDto> getItemsByOwner(Long userId) {
        return itemRepository.findByOwner_Id(userId).stream()
                .map(item -> new ItemOwnerDto(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getAvailable(),
                        findLastBooking(item.getId()),
                        findNextBooking(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasFinishedBooking = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasFinishedBooking) {
            throw new IllegalArgumentException("Оставить отзыв можно только после завершённой аренды вещи");
        }

        Comment comment = new Comment(null, commentDto.getText(), item, author, LocalDateTime.now());
        return toCommentDto(commentRepository.save(comment));
    }

    private ItemBookingDto findLastBooking(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByItem_IdAndStatus(itemId, BookingStatus.APPROVED).stream()
                .filter(booking -> booking.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(this::toItemBookingDto)
                .orElse(null);
    }

    private ItemBookingDto findNextBooking(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByItem_IdAndStatus(itemId, BookingStatus.APPROVED).stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(this::toItemBookingDto)
                .orElse(null);
    }

    private ItemBookingDto toItemBookingDto(Booking booking) {
        return new ItemBookingDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd());
    }

    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }
}