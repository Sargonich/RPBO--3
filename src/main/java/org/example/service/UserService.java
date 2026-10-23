package org.example.service;

import org.example.annotation.Cumponent;
import org.example.dto.UserRequest;
import org.example.factory.UserFactory;
import org.example.model.Team;
import org.example.model.User;
import org.example.repository.TeamRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Cumponent
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final UserFactory userFactory = new UserFactory();

    public UserService(
            UserRepository userRepository,
            TeamRepository teamRepository
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Пользователь не найден: " + id));
    }

    public User createUser(UserRequest request) {
        validateRequest(request);

        User user = userFactory.create(
                request.getUsername(),
                request.getEmail()
        );

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserRequest request) {
        validateRequest(request);

        User user = getUserById(id);

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    public User patchUser(Long id, UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        User user = getUserById(id);

        if (request.getUsername() != null) {
            validateUsername(request.getUsername());
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            validateEmail(request.getEmail());
            user.setEmail(request.getEmail());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);

        List<Team> teams = teamRepository.findAll();

        boolean isReferenced = teams.stream()
                .anyMatch(team ->
                        team.getCaptain() != null
                                && team.getCaptain().getId().equals(id)
                                || team.getMembers().stream()
                                .anyMatch(member ->
                                        member.getId().equals(id)));

        if (isReferenced) {
            throw new IllegalArgumentException(
                    "Нельзя удалить пользователя: он является капитаном "
                            + "или участником команды");
        }

        userRepository.deleteById(user.getId());
    }

    private void validateRequest(UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        validateUsername(request.getUsername());
        validateEmail(request.getEmail());
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Имя пользователя не может быть пустым");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Некорректный адрес электронной почты");
        }
    }
}