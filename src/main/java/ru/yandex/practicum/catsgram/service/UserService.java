package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.InvalidEmailException;
import ru.yandex.practicum.catsgram.exception.UserAlreadyExistException;
import ru.yandex.practicum.catsgram.exception.UserNotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong userIdGenerator = new AtomicLong(0);

    public Collection<User> findAllUsers() {
        return users.values();
    }

    public Optional<User> findUserById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(userId));
    }

    public User createUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new InvalidEmailException("Адрес электронной почты не может быть пустым.");
        }

        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new UserAlreadyExistException("Пользователь с электронной почтой " +
                    user.getEmail() + " уже зарегистрирован.");
        }

        user.setId(userIdGenerator.incrementAndGet());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new UserNotFoundException("Id пользователя не указан.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new InvalidEmailException("Адрес электронной почты не может быть пустым.");
        }

        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь с id = " + user.getId() + " не найден.");
        }

        users.put(user.getId(), user);
        return user;
    }
}
