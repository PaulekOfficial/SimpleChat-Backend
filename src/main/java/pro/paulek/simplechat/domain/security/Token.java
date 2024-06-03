package pro.paulek.simplechat.domain.security;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import pro.paulek.domain.user.UserCredentials;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserCredentials userCredentials;

    @NotNull
    @Column(length = 500, unique = true)
    private String token;

    private boolean expired;

    @Nullable
    private ZonedDateTime expiredTime;

    private boolean revoked;

    @Nullable
    private ZonedDateTime revokedTime;

    private ZonedDateTime timestamp;

    public Token() {
    }

    public Token(UserCredentials userCredentials, @NotNull String token, ZonedDateTime timestamp) {
        this.userCredentials = userCredentials;
        this.token = token;
        this.timestamp = timestamp;
    }

    public Token(Long id, UserCredentials userCredentials, @NotNull String token, boolean expired, @Nullable ZonedDateTime expiredTime, boolean revoked, @Nullable ZonedDateTime revokedTime, ZonedDateTime timestamp) {
        this.id = id;
        this.userCredentials = userCredentials;
        this.token = token;
        this.expired = expired;
        this.expiredTime = expiredTime;
        this.revoked = revoked;
        this.revokedTime = revokedTime;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserCredentials getUser() {
        return userCredentials;
    }

    public void setUser(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    @Nullable
    public ZonedDateTime getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(@Nullable ZonedDateTime expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    @Nullable
    public ZonedDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(@Nullable ZonedDateTime revokedTime) {
        this.revokedTime = revokedTime;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token1)) return false;
        return expired == token1.expired && revoked == token1.revoked && Objects.equals(id, token1.id) && Objects.equals(userCredentials, token1.userCredentials) && Objects.equals(token, token1.token) && Objects.equals(expiredTime, token1.expiredTime) && Objects.equals(revokedTime, token1.revokedTime) && Objects.equals(timestamp, token1.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userCredentials, token, expired, expiredTime, revoked, revokedTime, timestamp);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", user=" + userCredentials +
                ", token='" + token + '\'' +
                ", expired=" + expired +
                ", expiredTime=" + expiredTime +
                ", revoked=" + revoked +
                ", revokedTime=" + revokedTime +
                ", timestamp=" + timestamp +
                '}';
    }
}
