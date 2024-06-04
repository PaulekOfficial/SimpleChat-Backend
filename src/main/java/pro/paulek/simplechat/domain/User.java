package pro.paulek.simplechat.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String nickname;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    private Avatar avatar;

    private Instant createdAt;

    public User() {
    }

    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
    }

    public User(String nickname, String email, String password, Avatar avatar) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.createdAt = Instant.now();
    }

    public User(Long id, String nickname, String email, String password, Avatar avatar, Instant createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank @Size(max = 20) String getNickname() {
        return nickname;
    }

    public void setNickname(@NotBlank @Size(max = 20) String nickname) {
        this.nickname = nickname;
    }

    public @NotBlank @Size(max = 50) @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Size(max = 50) @Email String email) {
        this.email = email;
    }

    public @NotBlank @Size(max = 120) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(max = 120) String password) {
        this.password = password;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(nickname, user.nickname) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(avatar, user.avatar) && Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, email, password, avatar, createdAt);
    }
}
