package org.example.repository;

import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.filter.RoleFilter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {

    private final ConcurrentMap<String, Role> rolesById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Role> rolesByName = new ConcurrentHashMap<>();

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String id = role.getId();
        String name = role.getName();

        Role existingById = rolesById.putIfAbsent(id, role);
        if (existingById != null) {
            throw new IllegalArgumentException("Role with id '" + id + "' already exists");
        }

        Role existingByName = rolesByName.putIfAbsent(name, role);
        if (existingByName != null) {
            rolesById.remove(id, role);
            throw new IllegalArgumentException("Role with name '" + name + "' already exists");
        }
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) return false;

        Role removed = rolesById.get(item.getId());
        if (removed != null && rolesById.remove(item.getId(), removed)) {
            rolesByName.remove(removed.getName(), removed);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }

        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }
        synchronized (role) {
            role.addPermission(permission);
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }
        synchronized (role) {
            role.removePermission(permission);
        }
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(rolesById, that.rolesById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById);
    }
}