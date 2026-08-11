package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private static final String USER_FIELD = "user";
    private static final String OTHER_USER_FIELD = "other user";
    private static final String FRIEND_FIELD = "friend";
    private static final String USER_NOT_FOUND = "Пользователь не найден";

    @Getter
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(USER_FIELD, USER_NOT_FOUND));
        User friend = userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException(FRIEND_FIELD, USER_NOT_FOUND));

        Set<Long> userFriends = user.getFriends();
        userFriends.add(friendId);
        user.setFriends(userFriends);

        Set<Long> friendFriends = friend.getFriends();
        friendFriends.add(userId);
        friend.setFriends(friendFriends);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(USER_FIELD, USER_NOT_FOUND));
        User friend = userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException(FRIEND_FIELD, USER_NOT_FOUND));

        Set<Long> userFriends = user.getFriends();
        userFriends.remove(friendId);
        user.setFriends(userFriends);

        Set<Long> friendFriends = friend.getFriends();
        friendFriends.remove(userId);
        friend.setFriends(friendFriends);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(USER_FIELD, USER_NOT_FOUND));
        User otherUser = userStorage.findUserById(otherUserId)
                .orElseThrow(() -> new NotFoundException(OTHER_USER_FIELD, USER_NOT_FOUND));

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::findUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }


    public List<User> findAllFriends(long id) {
        User user = userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException(USER_FIELD, USER_NOT_FOUND));

        return user.getFriends().stream()
                .map(userStorage::findUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

