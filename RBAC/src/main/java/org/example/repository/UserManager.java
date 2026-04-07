package org.example.repository;

import org.example.entity.User;
import org.example.filter.UserFilter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {

    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        String username = user.username();
        User existing = users.putIfAbsent(username, user);
        if (existing != null) {
            throw new IllegalArgumentException("User with username '" + username + "' already exists");
        }
    }

    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }
        return users.remove(user.username(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return users.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<User> findAllParallel(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilterParallel(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User existing = users.get(username);
        if (existing == null) {
            throw new IllegalArgumentException("User with username '" + username + "' not found");
        }
        User updated = User.create(username, newFullName, newEmail);
        boolean replaced = users.replace(username, existing, updated);
        if (!replaced) {
            throw new IllegalStateException("User was modified concurrently, please retry");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}