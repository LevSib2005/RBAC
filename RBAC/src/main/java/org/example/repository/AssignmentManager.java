package org.example.repository;

import org.example.assignment.PermanentAssignment;
import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.filter.AssignmentFilter;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private Map<String, RoleAssignment> assignmentsById = new HashMap<>();
    private UserManager userManager;
    private RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }

        String id = assignment.assignmentId();

        if (assignmentsById.containsKey(id)) {
            throw new IllegalArgumentException("Assignment with id '" + id + "' already exists");
        }

        User user = assignment.user();
        Role role = assignment.role();

        if (!userManager.exists(user.username())) {
            throw new IllegalArgumentException("User '" + user.username() + "' does not exist");
        }

        if (!roleManager.exists(role.getName())) {
            throw new IllegalArgumentException("Role '" + role.getName() + "' does not exist");
        }

        boolean alreadyAssigned = assignmentsById.values().stream()
                .anyMatch(a -> a.user().equals(user) &&
                        a.role().equals(role) &&
                        a.isActive());

        if (alreadyAssigned) {
            throw new IllegalArgumentException("User '" + user.username() +
                    "' already has active assignment for role '" +
                    role.getName() + "'");
        }

        assignmentsById.put(id, assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        return assignmentsById.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignmentsById.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            return findAll();
        }

        return assignmentsById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);

        if (sorter != null) {
            result.sort(sorter);
        }

        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignmentsById.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignmentsById.values().stream()
                .anyMatch(a -> a.user().equals(user) &&
                        a.role().equals(role) &&
                        a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .anyMatch(a -> a.role().hasPermission(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> allPermissions = new HashSet<>();

        assignmentsById.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .forEach(a -> allPermissions.addAll(a.role().getPermissions()));

        return allPermissions;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);

        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");
        }

        if (assignment instanceof PermanentAssignment perm) {
            perm.revoke();
        } else {
            throw new IllegalArgumentException("Only permanent assignments can be revoked");
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);

        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");
        }

        if (assignment instanceof TemporaryAssignment temp) {
            temp.extend(newExpirationDate);
        } else {
            throw new IllegalArgumentException("Only temporary assignments can be extended");
        }
    }
}