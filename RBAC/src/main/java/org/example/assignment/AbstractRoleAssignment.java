package org.example.assignment;

import org.example.entity.AssignmentMetadata;
import org.example.entity.Role;
import org.example.entity.User;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {

    private String assignmentId;
    private User user;
    private Role role;
    private AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }

        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    public String summary() {
        String type = assignmentType();
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        String reason = metadata.reason() != null ? metadata.reason() : "No reason";

        return "[" + type + "] " + role.getName() + " assigned to " + user.username() +
                " by " + metadata.assignedBy() + " at " + metadata.assignedAt() +
                "\nReason: " + reason +
                "\nStatus: " + status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return assignmentId.equals(that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String toString() {
        return "AbstractRoleAssignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", user=" + user.username() +
                ", role=" + role.getName() +
                '}';
    }
}