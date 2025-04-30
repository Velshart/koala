package me.mmtr.koala.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String picture;
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_followers", joinColumns = {
            @JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
            @JoinColumn(name = "FOLLOWER_ID", referencedColumnName = "ID")}
    )
    private List<User> followers;

    public void addFollower(User user) {
        followers.add(user);
    }
    public void removeFollower(User user) {
        followers.remove(user);
    }

    public boolean isFollowedByUser(User userToCheck) {
        return followers.contains(userToCheck);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, picture, createdAt, followers);
    }
}
