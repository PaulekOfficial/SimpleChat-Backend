package pro.paulek.simplechat.domain.security;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import pro.paulek.simplechat.domain.User;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(length = 500, unique = true)
    private String refreshToken;

    private boolean expired;

    @Nullable
    private ZonedDateTime expiredTime;

    private boolean revoked;

    @Nullable
    private ZonedDateTime revokedTime;

    private ZonedDateTime timestamp;

    public RefreshToken() {
    }

    public RefreshToken(User user, @NotNull String refreshToken, ZonedDateTime timestamp) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.timestamp = timestamp;
    }

    public RefreshToken(Long id, User user, @NotNull String refreshToken, boolean expired, @Nullable ZonedDateTime expiredTime, boolean revoked, @Nullable ZonedDateTime revokedTime, ZonedDateTime timestamp) {
        this.id = id;
        this.user = user;
        this.refreshToken = refreshToken;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String token) {
        this.refreshToken = token;
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
        if (!(o instanceof RefreshToken token1)) return false;
        return expired == token1.expired && revoked == token1.revoked && Objects.equals(id, token1.id) && Objects.equals(user, token1.user) && Objects.equals(refreshToken, token1.refreshToken) && Objects.equals(expiredTime, token1.expiredTime) && Objects.equals(revokedTime, token1.revokedTime) && Objects.equals(timestamp, token1.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, refreshToken, expired, expiredTime, revoked, revokedTime, timestamp);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", user=" + user +
                ", token='" + refreshToken + '\'' +
                ", expired=" + expired +
                ", expiredTime=" + expiredTime +
                ", revoked=" + revoked +
                ", revokedTime=" + revokedTime +
                ", timestamp=" + timestamp +
                '}';
    }
}
